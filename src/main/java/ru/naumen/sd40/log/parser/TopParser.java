package ru.naumen.sd40.log.parser;

import org.springframework.web.multipart.MultipartFile;
import ru.naumen.sd40.log.parser.interfaceParsing.DataParser;
import ru.naumen.sd40.log.parser.interfaceParsing.TimeParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Top output parser
 * @author dkolmogortsev
 *
 */
public class TopParser implements DataParser, TimeParser
{

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHH:mm");

    private String dataDate;
    private MultipartFile multipartFile;

    private Map<Long, DataSet> existing;

    private Pattern timeRegex = Pattern.compile("^_+ (\\S+)");

    private Pattern cpuAndMemPattren = Pattern
            .compile("^ *\\d+ \\S+ +\\S+ +\\S+ +\\S+ +\\S+ +\\S+ +\\S+ \\S+ +(\\S+) +(\\S+) +\\S+ java");

    private DataSet currentSet;

    public TopParser(MultipartFile multipartFile, Map<Long, DataSet> existingDataSet, String timeZone) throws IllegalArgumentException
    {
        //Supports these masks in file name: YYYYmmdd, YYY-mm-dd i.e. 20161101, 2016-11-01
        Matcher matcher = Pattern.compile("\\d{8}|\\d{4}-\\d{2}-\\d{2}").matcher(multipartFile.getOriginalFilename());
        if (!matcher.find())
        {
            throw new IllegalArgumentException();
        }
        this.dataDate = matcher.group(0).replaceAll("-", "");
        this.multipartFile = multipartFile;
        this.existing = existingDataSet;
        sdf.setTimeZone(TimeZone.getTimeZone(timeZone));

    }

    public void parse() throws IOException, ParseException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(multipartFile.getInputStream())))
        {
            String line;
            while ((line = br.readLine()) != null)
            {
                long time = parseTime(line);
                if (time != 0) {
                    continue;
                }
                parseLine(line);
            }
        }
    }

    private long prepareDate(long parsedDate) {
        int min5 = 5 * 60 * 1000;
        long count = parsedDate / min5;
        return count * min5;
    }

    @Override
    public long parseTime(String line) throws ParseException {
        Matcher matcher = timeRegex.matcher(line);
        if (matcher.find())
        {
            long time = prepareDate(sdf.parse(dataDate + matcher.group(1)).getTime());
            currentSet = existing.computeIfAbsent(time, k -> new DataSet());
            return time;
        }
        return 0L;
    }

    @Override
    public void parseLine(String line) {
        Matcher la = Pattern.compile(".*load average:(.*)").matcher(line);
        if (la.find())
        {
            currentSet.cpuData().addLa(Double.parseDouble(la.group(1).split(",")[0].trim()));
            return;
        }

        //get cpu and mem
        Matcher cpuAndMemMatcher = cpuAndMemPattren.matcher(line);
        if (cpuAndMemMatcher.find())
        {
            currentSet.cpuData().addCpu(Double.valueOf(cpuAndMemMatcher.group(1)));
            currentSet.cpuData().addMem(Double.valueOf(cpuAndMemMatcher.group(2)));
            return;
        }
    }
}
