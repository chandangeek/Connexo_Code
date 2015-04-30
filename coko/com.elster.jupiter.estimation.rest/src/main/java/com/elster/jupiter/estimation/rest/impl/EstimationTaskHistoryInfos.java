package com.elster.jupiter.estimation.rest.impl;

import com.elster.jupiter.estimation.EstimationTask;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.tasks.TaskOccurrence;

import java.util.ArrayList;
import java.util.List;

public class EstimationTaskHistoryInfos {
    public int total;
    public List<EstimationTaskHistoryInfo> data = new ArrayList<>();

    public EstimationTaskHistoryInfos() {
    }

    public EstimationTaskHistoryInfos(EstimationTask task, Iterable<? extends TaskOccurrence> occurrences, Thesaurus thesaurus) {
        addAll(task, occurrences, thesaurus);
    }

    public EstimationTaskHistoryInfo add(EstimationTask task, TaskOccurrence occurrence, Thesaurus thesaurus) {
        EstimationTaskHistoryInfo result = new EstimationTaskHistoryInfo(task, occurrence, thesaurus);
        data.add(result);
        total++;
        return result;
    }

    private void addAll(EstimationTask task, Iterable<? extends TaskOccurrence> occurrences, Thesaurus thesaurus) {
        for (TaskOccurrence each : occurrences) {
            add(task, each, thesaurus);
        }
    }
}
