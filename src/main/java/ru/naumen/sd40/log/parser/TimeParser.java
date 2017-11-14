package ru.naumen.sd40.log.parser;

import org.springframework.web.multipart.MultipartFile;

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

/**
 * Created by doki on 22.10.16.
 */
public class TimeParser implements ru.naumen.sd40.log.parser.interfaceParsing.TimeParser
{
    private MultipartFile multipartFile;

    private Map<Long, DataSet> existing;

    private static final Pattern TIME_PATTERN = Pattern
            .compile("^\\d+ \\[.*?\\] \\((\\d{2} .{3} \\d{4} \\d{2}:\\d{2}:\\d{2},\\d{3})\\)");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd MMM yyyy HH:mm:ss,SSS",
            new Locale("ru", "RU"));

    public TimeParser(MultipartFile multipartFile, Map<Long, DataSet> existingDataSet, String zoneId) {
        this.multipartFile = multipartFile;
        this.existing = existingDataSet;
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone(zoneId));
    }

    public void parse() throws IOException, ParseException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(multipartFile.getInputStream())))
        {
            String line;
            while ((line = br.readLine()) != null)
            {
                long time = parseTime(line);

                if (time == 0) {
                    continue;
                }

                existing.computeIfAbsent(prepareDate(time), k -> new DataSet()).parseLine(line);
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
        Matcher matcher = TIME_PATTERN.matcher(line);

        if (matcher.find())
        {
            String timeString = matcher.group(1);
            Date recDate = DATE_FORMAT.parse(timeString);
            return recDate.getTime();
        }
        return 0L;
    }
}
