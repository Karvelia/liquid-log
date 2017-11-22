package ru.naumen.sd40.log.parser.timeParsing;

import ru.naumen.sd40.log.parser.interfaceParsing.TimeParser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeTopParser implements TimeParser {

    private Pattern TIME_PATTERN = Pattern.compile("^_+ (\\S+)");
    private SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddHH:mm");

    private String dataDate;
    private long time = 0L;

    public TimeTopParser(String timeZone, String fileName) {
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone(timeZone));

        //Supports these masks in file name: YYYYmmdd, YYY-mm-dd i.e. 20161101, 2016-11-01
        Matcher matcher = Pattern.compile("\\d{8}|\\d{4}-\\d{2}-\\d{2}").matcher(fileName);
        if (!matcher.find())
        {
            throw new IllegalArgumentException();
        }
        this.dataDate = matcher.group(0).replaceAll("-", "");
    }

    @Override
    public boolean parseTime(String line) throws ParseException {
        time = 0L;
        Matcher matcher = TIME_PATTERN.matcher(line);
        if (matcher.find())
        {
            String timeString = dataDate + matcher.group(1);
            Date recDate = DATE_FORMAT.parse(timeString);
            time = recDate.getTime();
            return true;
        }
        return false;
    }

    public long getTime() {
        return time;
    }
}
