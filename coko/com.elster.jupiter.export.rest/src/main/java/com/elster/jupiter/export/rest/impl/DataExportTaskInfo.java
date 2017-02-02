package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.time.rest.PeriodicalExpressionInfo;

import javax.xml.bind.annotation.XmlRootElement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class DataExportTaskInfo {

    public long id;
    public String name;
    public boolean active;
    public ProcessorInfo dataProcessor;
    public SelectorInfo dataSelector;
    public PeriodicalExpressionInfo schedule;
    public String recurrence;
    public DataExportTaskHistoryMinimalInfo lastExportOccurrence;
    public Instant nextRun;
    public Instant lastRun;
    public StandardDataSelectorInfo standardDataSelector;
    public List<DestinationInfo> destinations = new ArrayList<>();
    public long version;
    public int logLevel;
}
