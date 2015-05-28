package com.elster.jupiter.appserver.rest.impl;



import com.elster.jupiter.appserver.ImportScheduleOnAppServer;
import com.elster.jupiter.nls.Thesaurus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mandr on 5/25/2015.
 */
public class ImportScheduleInfos {
    public int total;
    public List<ImportScheduleInfo> importSchedules = new ArrayList<>();

    public ImportScheduleInfos(List<? extends ImportScheduleOnAppServer> importSchedules, Thesaurus thesaurus) {
        addAll(importSchedules,thesaurus);
    }

    public ImportScheduleInfo add(ImportScheduleOnAppServer importSchedule, Thesaurus thesaurus) {
        ImportScheduleInfo result = new ImportScheduleInfo(importSchedule, thesaurus);
        importSchedules.add(result);
        total++;
        return result;
    }

    public void addAll(Iterable<? extends ImportScheduleOnAppServer> importSchedules, Thesaurus thesaurus) {
        for (ImportScheduleOnAppServer each : importSchedules) {
            add(each, thesaurus);
        }
    }
}
