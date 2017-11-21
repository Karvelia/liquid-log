package ru.naumen.sd40.log.parser;

import ru.naumen.sd40.log.parser.dataParsing.DataGCParser;

/**
 * Created by doki on 22.10.16.
 */
public class DataSet
{
    private ActionDoneParser actionsDone;
    private ErrorParser errors;
    private DataGCParser gc;
    private TopData cpuData = new TopData();

    public DataSet()
    {
        actionsDone = new ActionDoneParser();
        errors = new ErrorParser();
        gc = new DataGCParser();
    }

    public void parseLine(String line)
    {
        errors.parseLine(line);
        actionsDone.parseLine(line);
    }

    public void parseGcLine(String line)
    {
        gc.parseLine(line);
    }

    public ActionDoneParser getActionsDone()
    {
        return actionsDone;
    }

    public ErrorParser getErrors()
    {
        return errors;
    }

    public DataGCParser getGc()
    {
        return gc;
    }

    public TopData cpuData()
    {
        return cpuData;
    }
}
