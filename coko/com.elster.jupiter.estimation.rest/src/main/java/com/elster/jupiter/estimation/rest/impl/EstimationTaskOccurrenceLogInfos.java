package com.elster.jupiter.estimation.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.logging.LogEntry;

import java.util.ArrayList;
import java.util.List;

public class EstimationTaskOccurrenceLogInfos {
    public int total;
    public List<EstimationTaskOccurrenceLogInfo> data = new ArrayList<>();

    public EstimationTaskOccurrenceLogInfos() {
    }

    public EstimationTaskOccurrenceLogInfos(Iterable<? extends LogEntry> logEntries, Thesaurus thesaurus) {
        addAll(logEntries, thesaurus);
    }

    public EstimationTaskOccurrenceLogInfo add(LogEntry logEntry, Thesaurus thesaurus) {
        EstimationTaskOccurrenceLogInfo result = new EstimationTaskOccurrenceLogInfo(logEntry, thesaurus);
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
