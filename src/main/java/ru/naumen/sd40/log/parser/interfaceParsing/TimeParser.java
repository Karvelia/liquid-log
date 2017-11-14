package ru.naumen.sd40.log.parser.interfaceParsing;

import java.text.ParseException;
import java.util.Date;
import java.util.regex.Matcher;

public interface TimeParser {
    long parseTime(String line) throws ParseException;
}
