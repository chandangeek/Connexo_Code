/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.rest;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.logging.LogEntry;

public class DataValidationOccurrenceLogInfo {
    public Long timestamp;
    public String loglevel;
    public String message;

    public DataValidationOccurrenceLogInfo() {}

    public DataValidationOccurrenceLogInfo(LogEntry logEntry, Thesaurus thesaurus) {
        this.timestamp = logEntry.getTimestamp().toEpochMilli();
        this.loglevel = logEntry.getLogLevel().getName();
        this.message = logEntry.getMessage();
    }
}
