/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.share.entity;

import java.time.Instant;

public class DueDateRange {
    private long startTime;
    private long endTime;

    public DueDateRange(long startTime, long endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public Instant getStartTimeAsInstant() {
        return Instant.ofEpochMilli(startTime);
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public Instant getEndTimeAsInstant() {
        return Instant.ofEpochMilli(endTime);
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
}
