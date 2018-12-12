/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.customtask.rest.impl;

import java.time.Instant;

public class CustomTaskHistoryMinimalInfo {

    public Long id;
    public String trigger;
    public Instant startedOn;
    public Instant finishedOn;
    public Long duration;
    public String statusType;
    public String status;
    public String reason;
    public Instant lastRun;
    public Instant statusDate;
    public String statusPrefix;
    public Boolean wasScheduled;

}