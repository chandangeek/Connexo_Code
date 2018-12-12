/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.customtask.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.logging.LogEntry;

import java.util.ArrayList;
import java.util.List;

public class CustomTaskOccurrenceLogInfos {
    public int total;
    public List<CustomTaskOccurrenceLogInfo> data = new ArrayList<>();

    public CustomTaskOccurrenceLogInfos() {
    }

    public CustomTaskOccurrenceLogInfos(Iterable<? extends LogEntry> logEntries, Thesaurus thesaurus) {
        addAll(logEntries, thesaurus);
    }

    public CustomTaskOccurrenceLogInfo add(LogEntry logEntry) {
        CustomTaskOccurrenceLogInfo result = CustomTaskOccurrenceLogInfo.from(logEntry);
        data.add(result);
        total++;
        return result;
    }

    public void addAll(Iterable<? extends LogEntry> logEntries, Thesaurus thesaurus) {
        for (LogEntry each : logEntries) {
            add(each);
        }
    }
}
