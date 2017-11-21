package ru.naumen.sd40.log.parser.interfaceParsing;

import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;

public interface DataParser {
    void parseLine(long time, String line);
    void setCurrentSet(long time);

    default long prepareDate(long parsedDate) {
        int min5 = 5 * 60 * 1000;
        long count = parsedDate / min5;
        return count * min5;
    }
    default void parse(TimeParser timeParser, MultipartFile multipartFile) throws IOException, ParseException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(multipartFile.getInputStream())))
        {
            String line;
            while ((line = br.readLine()) != null)
            {
                if (timeParser.parseTime(line))
                {
                    setCurrentSet(prepareDate(timeParser.getTime()));
                    continue;
                }
                parseLine(prepareDate(timeParser.getTime()), line);
            }
        }
    }
}
