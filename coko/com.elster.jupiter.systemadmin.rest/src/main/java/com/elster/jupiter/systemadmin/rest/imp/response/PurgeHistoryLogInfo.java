/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.systemadmin.rest.imp.response;

import com.elster.jupiter.util.logging.LogEntry;

public class PurgeHistoryLogInfo {
    public Long timestamp;
    public String logLevel;
    public String message;

    public PurgeHistoryLogInfo() {}

    public PurgeHistoryLogInfo(LogEntry logEntry) {
        if (logEntry != null) {
            this.timestamp = logEntry.getTimestamp().toEpochMilli();
            this.logLevel = logEntry.getLogLevel().getName();
            this.message = logEntry.getMessage();
        }
    }
}
