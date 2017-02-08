/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.logging.LogEntry;

import java.util.ArrayList;
import java.util.List;

public class DataExportOccurrenceLogInfos {
    public int total;
    public List<DataExportOccurrenceLogInfo> data = new ArrayList<>();

    public DataExportOccurrenceLogInfos() {
    }

    public DataExportOccurrenceLogInfos(Iterable<? extends LogEntry> logEntries, Thesaurus thesaurus) {
        addAll(logEntries, thesaurus);
    }

    public DataExportOccurrenceLogInfo add(LogEntry logEntry, Thesaurus thesaurus) {
        DataExportOccurrenceLogInfo result = new DataExportOccurrenceLogInfo(logEntry, thesaurus);
        data.add(result);
        total++;
        return result;
    }

    public void addAll(Iterable<? extends LogEntry> logEntries, Thesaurus thesaurus) {
        for (LogEntry each : logEntries) {
            add(each, thesaurus);
        }
    }
}
