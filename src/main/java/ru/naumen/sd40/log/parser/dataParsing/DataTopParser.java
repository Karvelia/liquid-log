package ru.naumen.sd40.log.parser.dataParsing;

import ru.naumen.sd40.log.parser.DataSet;
import ru.naumen.sd40.log.parser.interfaceParsing.DataParser;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataTopParser implements DataParser {

    private Map<Long, DataSet> existing;
    DataSet currentSet;

    private Pattern CPU_AND_MEM_PATTERN = Pattern
            .compile("^ *\\d+ \\S+ +\\S+ +\\S+ +\\S+ +\\S+ +\\S+ +\\S+ \\S+ +(\\S+) +(\\S+) +\\S+ java");

    public DataTopParser(Map<Long, DataSet> existingDataSet) throws IllegalArgumentException
    {
        this.existing = existingDataSet;
    }

    @Override
    public void parseLine(long time, String line) {
        if (currentSet != null) {
            Matcher la = Pattern.compile(".*load average:(.*)").matcher(line);
            if (la.find()) {
                currentSet.cpuData().addLa(Double.parseDouble(la.group(1).split(",")[0].trim()));
                return;
            }

            //get cpu and mem
            Matcher cpuAndMemMatcher = CPU_AND_MEM_PATTERN.matcher(line);
            if (cpuAndMemMatcher.find()) {
                currentSet.cpuData().addCpu(Double.valueOf(cpuAndMemMatcher.group(1)));
                currentSet.cpuData().addMem(Double.valueOf(cpuAndMemMatcher.group(2)));
                return;
            }
        }
    }

    @Override
    public void setCurrentSet(long time) {
        currentSet = existing.computeIfAbsent(time, k -> new DataSet());
    }
}
