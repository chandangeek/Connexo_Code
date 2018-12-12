/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fileimport.rest.impl;

import com.elster.jupiter.util.logging.LogEntry;

/**
 * Created by Lucian on 6/2/2015.
 */
public class ImportLogEntryInfo {

    public Long timestamp;
    public String loglevel;
    public String message;

    public ImportLogEntryInfo() {}

    public ImportLogEntryInfo(LogEntry logEntry) {
        this();
        this.timestamp = logEntry.getTimestamp().toEpochMilli();
        this.loglevel = logEntry.getLogLevel().getName();
        this.message = logEntry.getMessage();
    }
}
