package ru.naumen.sd40.log.parser.interfaceParsing;

import java.io.IOException;
import java.text.ParseException;

public interface BaseParser {
    void parse() throws IOException, ParseException;
}
