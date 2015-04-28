package com.elster.jupiter.estimation.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.logging.LogEntry;

public class EstimationTaskOccurrenceLogInfo {
    public Long timestamp;
    public String loglevel;
    public String message;

    public EstimationTaskOccurrenceLogInfo() {
    }

    public EstimationTaskOccurrenceLogInfo(LogEntry logEntry, Thesaurus thesaurus) {
        this.timestamp = logEntry.getTimestamp().toEpochMilli();
        this.loglevel = logEntry.getLogLevel().getName();
        this.message = logEntry.getMessage();
    }
}
