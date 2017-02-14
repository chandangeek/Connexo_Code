/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.rest;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.logging.LogEntry;

import java.util.ArrayList;
import java.util.List;

public class DataValidationOccurrenceLogInfos {
    public int total;
    public List<DataValidationOccurrenceLogInfo> data = new ArrayList<>();

    public DataValidationOccurrenceLogInfos() {
    }

    public DataValidationOccurrenceLogInfos(Iterable<? extends LogEntry> logEntries, Thesaurus thesaurus) {
        addAll(logEntries, thesaurus);
    }

    public DataValidationOccurrenceLogInfo add(LogEntry logEntry, Thesaurus thesaurus) {
        DataValidationOccurrenceLogInfo result = new DataValidationOccurrenceLogInfo(logEntry, thesaurus);
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
