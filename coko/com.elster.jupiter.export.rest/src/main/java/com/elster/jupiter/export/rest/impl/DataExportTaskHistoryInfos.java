package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.ReadingTypeDataExportTask;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.History;
import com.elster.jupiter.time.TimeService;

import java.util.ArrayList;
import java.util.List;

public class DataExportTaskHistoryInfos {
    public int total;
    public List<DataExportTaskHistoryInfo> data = new ArrayList<>();

    public DataExportTaskHistoryInfos() {
    }

    public DataExportTaskHistoryInfos(ReadingTypeDataExportTask task, Iterable<? extends DataExportOccurrence> occurrences, Thesaurus thesaurus, TimeService timeService) {
        addAll(task, occurrences, thesaurus, timeService);
    }

    public DataExportTaskHistoryInfo add(DataExportOccurrence occurrence, Thesaurus thesaurus, TimeService timeService) {
        DataExportTaskHistoryInfo result = new DataExportTaskHistoryInfo(occurrence, thesaurus, timeService);
        data.add(result);
        total++;
        return result;
    }

    public DataExportTaskHistoryInfo add(History<? extends ReadingTypeDataExportTask> history, DataExportOccurrence occurrence, Thesaurus thesaurus, TimeService timeService) {
        DataExportTaskHistoryInfo result = new DataExportTaskHistoryInfo(history, occurrence, thesaurus, timeService);
        data.add(result);
        total++;
        return result;
    }

    private void addAll(ReadingTypeDataExportTask task, Iterable<? extends DataExportOccurrence> occurrences, Thesaurus thesaurus, TimeService timeService) {
        History<? extends ReadingTypeDataExportTask> history = task.getHistory();
        for (DataExportOccurrence each : occurrences) {
            add(history, each, thesaurus, timeService);
        }
    }
}
