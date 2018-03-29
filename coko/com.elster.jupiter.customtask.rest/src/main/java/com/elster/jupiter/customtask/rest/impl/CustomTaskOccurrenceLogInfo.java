/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.customtask.rest.impl;

import com.elster.jupiter.util.logging.LogEntry;

public class CustomTaskOccurrenceLogInfo {
    public Long timestamp;
    public int loglevel;
    public String message;

    public CustomTaskOccurrenceLogInfo() {
    }

    public static CustomTaskOccurrenceLogInfo from(LogEntry logEntry) {
        CustomTaskOccurrenceLogInfo occurrenceLogInfo = new CustomTaskOccurrenceLogInfo();
        occurrenceLogInfo.timestamp = logEntry.getTimestamp().toEpochMilli();
        occurrenceLogInfo.loglevel = logEntry.getLogLevel().intValue();
        occurrenceLogInfo.message = logEntry.getMessage();
        return occurrenceLogInfo;
    }
}
