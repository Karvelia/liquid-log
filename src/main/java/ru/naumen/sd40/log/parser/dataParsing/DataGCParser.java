package ru.naumen.sd40.log.parser.dataParsing;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import ru.naumen.sd40.log.parser.DataSet;
import ru.naumen.sd40.log.parser.interfaceParsing.DataParser;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ru.naumen.sd40.log.parser.NumberUtils.getSafeDouble;
import static ru.naumen.sd40.log.parser.NumberUtils.roundToTwoPlaces;

public class DataGCParser implements DataParser {

    private Map<Long, DataSet> existing;

    public DataGCParser() {

    }

    public DataGCParser(Map<Long, DataSet> existingDataSet) throws IllegalArgumentException {
        this.existing = existingDataSet;
    }

    @Override
    public void parseLine(long time, String line) {
        existing.computeIfAbsent(time, k -> new DataSet()).parseGcLine(line);
    }

    @Override
    public void setCurrentSet(long time) {

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

    public void parseLine(String line) {
        Matcher matcher = gcExecutionTime.matcher(line);
        if (matcher.find())
        {
            ds.addValue(Double.parseDouble(matcher.group(1).trim().replace(',', '.')));
        }
    }
}
