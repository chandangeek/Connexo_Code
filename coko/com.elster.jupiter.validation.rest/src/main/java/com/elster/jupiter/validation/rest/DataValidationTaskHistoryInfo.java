/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.rest;

import java.time.Instant;

public class DataValidationTaskHistoryInfo {

    public Long id;
    public Instant startedOn;
    public Instant finishedOn;
    public Long duration;
    public String status;
    public String reason;
    public Instant lastRun;
    public Boolean wasScheduled;
    public Instant statusDate;
    public String statusPrefix;
    public DataValidationTaskInfo task;

}
