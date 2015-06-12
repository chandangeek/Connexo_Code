package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.export.ExportTask;
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

    public DataExportTaskHistoryInfos(ExportTask task, Iterable<? extends DataExportOccurrence> occurrences, Thesaurus thesaurus, TimeService timeService, PropertyUtils propertyUtils) {
        addAll(task, occurrences, thesaurus, timeService, propertyUtils);
    }

    private DataExportTaskHistoryInfo add(DataExportOccurrence occurrence, Thesaurus thesaurus, TimeService timeService, PropertyUtils propertyUtils) {
        DataExportTaskHistoryInfo result = new DataExportTaskHistoryInfo(occurrence, thesaurus, timeService, propertyUtils);
        data.add(result);
        total++;
        return result;
    }

    public DataExportTaskHistoryInfo add(History<ExportTask> history, DataExportOccurrence occurrence, Thesaurus thesaurus, TimeService timeService, PropertyUtils propertyUtils) {
        DataExportTaskHistoryInfo result = new DataExportTaskHistoryInfo(history, occurrence, thesaurus, timeService, propertyUtils);
        data.add(result);
        total++;
        return result;
    }

    private void addAll(ExportTask task, Iterable<? extends DataExportOccurrence> occurrences, Thesaurus thesaurus, TimeService timeService, PropertyUtils propertyUtils) {
        History<ExportTask> history = task.getHistory();
        for (DataExportOccurrence each : occurrences) {
            add(history, each, thesaurus, timeService, propertyUtils);
        }
    }
}
