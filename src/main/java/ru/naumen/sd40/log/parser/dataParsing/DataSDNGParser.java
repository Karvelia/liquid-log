package ru.naumen.sd40.log.parser.dataParsing;

import ru.naumen.sd40.log.parser.DataSet;
import ru.naumen.sd40.log.parser.interfaceParsing.DataParser;

import java.util.Map;

public class DataSDNGParser implements DataParser {

    private Map<Long, DataSet> existing;

    public DataSDNGParser(Map<Long, DataSet> existingDataSet) {
        this.existing = existingDataSet;
    }
    @Override
    public void parseLine(long time, String line) {
        existing.computeIfAbsent(time, k -> new DataSet()).parseLine(line);
    }

    @Override
    public void setCurrentSet(long time) {

    }
}
