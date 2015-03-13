package com.elster.jupiter.estimation.rest.impl;

import com.elster.jupiter.estimation.EstimationTask;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.History;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.time.TimeService;

import java.util.ArrayList;
import java.util.List;

public class EstimationTaskHistoryInfos {
    public int total;
    public List<EstimationTaskHistoryInfo> data = new ArrayList<>();

    public EstimationTaskHistoryInfos() {
    }

    public EstimationTaskHistoryInfos(EstimationTask task, Iterable<? extends TaskOccurrence> occurrences, Thesaurus thesaurus, TimeService timeService) {
        addAll(task, occurrences, thesaurus, timeService);
    }

    public EstimationTaskHistoryInfo add(History<? extends EstimationTask> history, TaskOccurrence occurrence, Thesaurus thesaurus, TimeService timeService) {
        EstimationTaskHistoryInfo result = new EstimationTaskHistoryInfo(history, occurrence, thesaurus, timeService);
        data.add(result);
        total++;
        return result;
    }

    private void addAll(EstimationTask task, Iterable<? extends TaskOccurrence> occurrences, Thesaurus thesaurus, TimeService timeService) {
        History<? extends EstimationTask> history = task.getHistory();
        for (TaskOccurrence each : occurrences) {
            add(history, each, thesaurus, timeService);
        }
    }
}
