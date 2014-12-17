package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.export.DataExportOccurrence;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.time.TimeService;

import java.util.ArrayList;
import java.util.List;

public class DataExportTaskHistoryInfos {
    public int total;
    public List<DataExportTaskHistoryInfo> data = new ArrayList<>();

    public DataExportTaskHistoryInfos() {
    }

    public DataExportTaskHistoryInfos(Iterable<? extends DataExportOccurrence> occurrences, Thesaurus thesaurus, TimeService timeService) {
        addAll(occurrences, thesaurus, timeService);
    }

    public DataExportTaskHistoryInfo add(DataExportOccurrence occurrence, Thesaurus thesaurus, TimeService timeService) {
        DataExportTaskHistoryInfo result = new DataExportTaskHistoryInfo(occurrence, thesaurus, timeService);
        data.add(result);
        total++;
        return result;
    }

    public void addAll(Iterable<? extends DataExportOccurrence> occurrences, Thesaurus thesaurus, TimeService timeService) {
        for (DataExportOccurrence each : occurrences) {
            add(each, thesaurus, timeService);
        }
    }
}
