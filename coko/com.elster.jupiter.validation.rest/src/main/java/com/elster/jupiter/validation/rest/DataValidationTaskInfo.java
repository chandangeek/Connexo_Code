package com.elster.jupiter.validation.rest;

import com.elster.jupiter.rest.util.IdWithDisplayValueInfo;
import com.elster.jupiter.time.rest.PeriodicalExpressionInfo;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class DataValidationTaskInfo {

    public long id;
    public String name;
    public IdWithDisplayValueInfo<Long> deviceGroup;
    public IdWithDisplayValueInfo metrologyConfiguration;
    public IdWithDisplayValueInfo<Long> metrologyContract;
    public String recurrence;
    public PeriodicalExpressionInfo schedule;
    public DataValidationTaskHistoryInfo lastValidationOccurence;
    public Long nextRun;
    public Long lastRun;
    public long version;

}
