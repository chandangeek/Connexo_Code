package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.logging.LogEntry;

public class DataExportOccurrenceLogInfo {
    public Long timestamp;
    public int loglevel;
    public String message;

    public DataExportOccurrenceLogInfo() {}

    public DataExportOccurrenceLogInfo(LogEntry logEntry, Thesaurus thesaurus) {
        this.timestamp = logEntry.getTimestamp().toEpochMilli();
        this.loglevel = logEntry.getLogLevel().intValue();
        this.message = logEntry.getMessage();
    }
}
