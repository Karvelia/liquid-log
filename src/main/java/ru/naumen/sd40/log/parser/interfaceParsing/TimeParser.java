package ru.naumen.sd40.log.parser.interfaceParsing;

import java.text.ParseException;

public interface TimeParser {
    boolean parseTime(String line) throws ParseException;
    long getTime();
}
