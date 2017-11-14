package ru.naumen.sd40.log.parser;

import static ru.naumen.sd40.log.parser.NumberUtils.getSafeDouble;
import static ru.naumen.sd40.log.parser.NumberUtils.roundToTwoPlaces;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.springframework.web.multipart.MultipartFile;
import ru.naumen.sd40.log.parser.interfaceParsing.DataParser;
import ru.naumen.sd40.log.parser.interfaceParsing.TimeParser;

public class GCParser implements DataParser, TimeParser
{
    private MultipartFile multipartFile;

    private Map<Long, DataSet> existing;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ",
            new Locale("ru", "RU"));

    private static final Pattern PATTERN = Pattern
            .compile("^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{3}\\+\\d{4}).*");

    public GCParser() {

    }

    public GCParser(MultipartFile multipartFile, Map<Long, DataSet> existingDataSet, String timeZone) throws IllegalArgumentException {
        this.multipartFile = multipartFile;
        this.existing = existingDataSet;
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone(timeZone));
    }

    public void parse() throws IOException, ParseException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(multipartFile.getInputStream())))
        {
            String line;
            while ((line = br.readLine()) != null)
            {
                long time = parseTime(line);

                if (time == 0)
                {
                    continue;
                }

                existing.computeIfAbsent(prepareDate(time), k -> new DataSet()).parseGcLine(line);
            }
        }
    }

    @Override
    public long parseTime(String line) throws ParseException {
        Matcher matcher = PATTERN.matcher(line);
        if (matcher.find())
        {
            Date parse = DATE_FORMAT.parse(matcher.group(1));
            return parse.getTime();
        }
        return 0L;
    }

    private long prepareDate(long parsedDate) {
        int min5 = 5 * 60 * 1000;
        long count = parsedDate / min5;
        return count * min5;
    }

    private DescriptiveStatistics ds = new DescriptiveStatistics();

    private Pattern gcExecutionTime = Pattern.compile(".*real=(.*)secs.*");

    public double getCalculatedAvg()
    {
        return roundToTwoPlaces(getSafeDouble(ds.getMean()));
    }

    public long getGcTimes()
    {
        return ds.getN();
    }

    public double getMaxGcTime()
    {
        return roundToTwoPlaces(getSafeDouble(ds.getMax()));
    }

    public boolean isNan()
    {
        return getGcTimes() == 0;
    }

    @Override
    public void parseLine(String line) {
        Matcher matcher = gcExecutionTime.matcher(line);
        if (matcher.find())
        {
            ds.addValue(Double.parseDouble(matcher.group(1).trim().replace(',', '.')));
        }
    }
}
