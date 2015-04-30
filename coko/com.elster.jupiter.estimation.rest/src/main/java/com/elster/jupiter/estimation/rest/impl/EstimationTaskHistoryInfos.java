package com.elster.jupiter.estimation.rest.impl;

import com.elster.jupiter.estimation.EstimationTask;
import com.elster.jupiter.estimation.EstimationTaskOccurrence;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.History;
import com.elster.jupiter.time.TimeService;

import java.util.ArrayList;
import java.util.List;

public class EstimationTaskHistoryInfos {
    public int total;
    public List<EstimationTaskHistoryInfo> data = new ArrayList<>();

    public EstimationTaskHistoryInfos() {
    }

    public EstimationTaskHistoryInfos(EstimationTask task, Iterable<? extends EstimationTaskOccurrence> occurrences, Thesaurus thesaurus) {
        addAll(task, occurrences, thesaurus);
    }

    public EstimationTaskHistoryInfo add(History<? extends EstimationTask> history, EstimationTaskOccurrence occurrence, Thesaurus thesaurus) {
        EstimationTaskHistoryInfo result = new EstimationTaskHistoryInfo(history, occurrence, thesaurus);
        data.add(result);
        total++;
        return result;
    }

    private void addAll(EstimationTask task, Iterable<? extends EstimationTaskOccurrence> occurrences, Thesaurus thesaurus) {
        History<? extends EstimationTask> history = task.getHistory();
        for (EstimationTaskOccurrence each : occurrences) {
            add(history, each, thesaurus);
        }
    }
}
