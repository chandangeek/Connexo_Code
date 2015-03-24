package com.elster.jupiter.validation.rest;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.History;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.validation.DataValidationOccurrence;
import com.elster.jupiter.validation.DataValidationTask;

import java.util.ArrayList;
import java.util.List;

public class DataValidationTaskHistoryInfos {
    public int total;
    public List<DataValidationTaskHistoryInfo> data = new ArrayList<>();

    public DataValidationTaskHistoryInfos() {
    }

    public DataValidationTaskHistoryInfos(DataValidationTask task, Iterable<? extends DataValidationOccurrence> occurrences, Thesaurus thesaurus, TimeService timeService) {
        addAll(task, occurrences, thesaurus, timeService);
    }

    public DataValidationTaskHistoryInfo add(DataValidationOccurrence occurrence, Thesaurus thesaurus) {
        DataValidationTaskHistoryInfo result = new DataValidationTaskHistoryInfo(occurrence, thesaurus);
        data.add(result);
        total++;
        return result;
    }

    public DataValidationTaskHistoryInfo add(History<? extends DataValidationTask> history, DataValidationOccurrence occurrence, Thesaurus thesaurus) {
        DataValidationTaskHistoryInfo result = new DataValidationTaskHistoryInfo(history, occurrence, thesaurus);
        data.add(result);
        total++;
        return result;
    }

    private void addAll(DataValidationTask task, Iterable<? extends DataValidationOccurrence> occurrences, Thesaurus thesaurus, TimeService timeService) {
        History<? extends DataValidationTask> history = task.getHistory();
        for (DataValidationOccurrence each : occurrences) {
            add(history, each, thesaurus);
        }
    }
}
