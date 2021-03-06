package ru.naumen.sd40.log.parser;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;

import org.influxdb.dto.BatchPoints;

import org.springframework.web.multipart.MultipartFile;
import ru.naumen.perfhouse.influx.InfluxDAO;
import ru.naumen.sd40.log.parser.dataParsing.DataGCParser;
import ru.naumen.sd40.log.parser.dataParsing.DataSDNGParser;
import ru.naumen.sd40.log.parser.dataParsing.DataTopParser;
import ru.naumen.sd40.log.parser.interfaceParsing.DataParser;
import ru.naumen.sd40.log.parser.interfaceParsing.TimeParser;
import ru.naumen.sd40.log.parser.timeParsing.TimeGCParser;
import ru.naumen.sd40.log.parser.timeParsing.TimeSDNGParser;
import ru.naumen.sd40.log.parser.timeParsing.TimeTopParser;

/**
 * Created by doki on 22.10.16.
 */
public class Parser
{
    /**
     *
     * @param multipartFile - файл с логами
     * @param nameDB - имя базы данных
     * @param timeZone - часовой пояс
     * @param parseMode - тип парсинга (top/sdng/gc)
     * @param logCheck - true - выводить в консоль результат лога
     * @param influxDAO - переменная для работы с базой данных
     * @throws IOException -
     * @throws ParseException -
     */
    public static void parse(MultipartFile multipartFile, String nameDB, String timeZone, String parseMode, boolean logCheck, InfluxDAO influxDAO) throws IOException, ParseException
    {
        String influxDb = nameDB.replaceAll("-", "_");
        influxDAO.init();
        influxDAO.connectToDB(influxDb);
        BatchPoints points = influxDAO.startBatchPoints(influxDb);

        HashMap<Long, DataSet> data = new HashMap<>();

        DataParser dataParser;
        TimeParser timeParser;
        switch (parseMode)
        {
        case "sdng":
            dataParser = new DataSDNGParser(data);
            timeParser = new TimeSDNGParser(timeZone);
            break;
        case "gc":
            dataParser = new DataGCParser(data);
            timeParser = new TimeGCParser(timeZone);
            break;
        case "top":
            dataParser = new DataTopParser(data);
            timeParser = new TimeTopParser(timeZone, multipartFile.getOriginalFilename());
            break;
        default:
            throw new IllegalArgumentException(
                    "Unknown parse parseMode! Availiable modes: sdng, gc, top. Requested parseMode: " + parseMode);
        }
        dataParser.parse(timeParser,multipartFile);

        if (logCheck)
        {
            System.out.print("Timestamp;Actions;Min;Mean;Stddev;50%%;95%%;99%%;99.9%%;Max;Errors\n");
        }
        data.forEach((k, set) ->
        {
            ActionDoneParser dones = set.getActionsDone();
            dones.calculate();
            ErrorParser erros = set.getErrors();
            if (logCheck)
            {
                System.out.print(String.format("%d;%d;%f;%f;%f;%f;%f;%f;%f;%f;%d\n", k, dones.getCount(),
                        dones.getMin(), dones.getMean(), dones.getStddev(), dones.getPercent50(), dones.getPercent95(),
                        dones.getPercent99(), dones.getPercent999(), dones.getMax(), erros.getErrorCount()));
            }
            if (!dones.isNan())
            {
                influxDAO.storeActionsFromLog(points, influxDb, k, dones, erros);
            }

            DataGCParser gc = set.getGc();
            if (!gc.isNan())
            {
                influxDAO.storeGc(points, influxDb, k, gc);
            }

            TopData cpuData = set.cpuData();
            if (!cpuData.isNan())
            {
                influxDAO.storeTop(points, influxDb, k, cpuData);
            }
        });
        influxDAO.writeBatch(points);
    }
}
