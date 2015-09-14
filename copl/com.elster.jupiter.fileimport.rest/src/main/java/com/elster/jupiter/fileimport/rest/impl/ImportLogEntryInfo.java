package com.elster.jupiter.fileimport.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.logging.LogEntry;

/**
 * Created by Lucian on 6/2/2015.
 */
public class ImportLogEntryInfo {

    public Long timestamp;
    public String loglevel;
    public String message;

    public ImportLogEntryInfo() {}

    public ImportLogEntryInfo(LogEntry logEntry, Thesaurus thesaurus) {
        this.timestamp = logEntry.getTimestamp().toEpochMilli();
        this.loglevel = logEntry.getLogLevel().getName();
        this.message = logEntry.getMessage();
    }
}
