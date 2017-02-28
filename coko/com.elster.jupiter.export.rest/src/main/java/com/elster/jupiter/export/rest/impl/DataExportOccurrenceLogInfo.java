/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.util.logging.LogEntry;

public class DataExportOccurrenceLogInfo {
    public Long timestamp;
    public int loglevel;
    public String message;

    public DataExportOccurrenceLogInfo() {}

    public static DataExportOccurrenceLogInfo from(LogEntry logEntry) {
        DataExportOccurrenceLogInfo dataExportOccurrenceLogInfo = new DataExportOccurrenceLogInfo();
        dataExportOccurrenceLogInfo.timestamp = logEntry.getTimestamp().toEpochMilli();
        dataExportOccurrenceLogInfo.loglevel = logEntry.getLogLevel().intValue();
        dataExportOccurrenceLogInfo.message = logEntry.getMessage();
        return dataExportOccurrenceLogInfo;
    }
}
