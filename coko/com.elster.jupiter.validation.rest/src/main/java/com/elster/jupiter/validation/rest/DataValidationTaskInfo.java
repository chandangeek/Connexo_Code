package com.elster.jupiter.validation.rest;

import com.elster.jupiter.rest.util.IdWithDisplayValueInfo;

import javax.xml.bind.annotation.XmlRootElement;
import java.time.Instant;

@XmlRootElement
public class DataValidationTaskInfo extends DataValidationTaskMinimalInfo {

    public IdWithDisplayValueInfo<Long> deviceGroup;
    public IdWithDisplayValueInfo metrologyConfiguration;
    public IdWithDisplayValueInfo<Long> metrologyContract;
    public String recurrence;
    public DataValidationTaskHistoryInfo lastValidationOccurence;
    public Instant lastRun;
    public long version;

}
