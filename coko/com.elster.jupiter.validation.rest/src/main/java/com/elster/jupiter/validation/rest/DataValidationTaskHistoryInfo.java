package com.elster.jupiter.validation.rest;

public class DataValidationTaskHistoryInfo {

    public Long id;
    public Long startedOn;
    public Long finishedOn;
    public Long duration;
    public String status;
    public String reason;
    public Long lastRun;
    public Long statusDate;
    public String statusPrefix;
    public DataValidationTaskInfo task;

}
