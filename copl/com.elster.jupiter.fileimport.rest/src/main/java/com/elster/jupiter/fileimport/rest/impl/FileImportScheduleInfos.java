package com.elster.jupiter.fileimport.rest.impl;

import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.nls.Thesaurus;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lucian on 5/14/2015.
 */

@XmlRootElement
public class FileImportScheduleInfos {
    public int total;
    public List<FileImportScheduleInfo> importSchedules = new ArrayList<>();

    public FileImportScheduleInfos(List<? extends ImportSchedule> importSchedules, Thesaurus thesaurus) {
        addAll(importSchedules,thesaurus);
    }
    public FileImportScheduleInfo add(ImportSchedule importSchedule, Thesaurus thesaurus) {
        FileImportScheduleInfo result = new FileImportScheduleInfo(importSchedule, thesaurus);
        importSchedules.add(result);
        total++;
        return result;
    }

    public FileImportScheduleInfos() {
    }


    public void addAll(Iterable<? extends ImportSchedule> importSchedules, Thesaurus thesaurus) {
        for (ImportSchedule each : importSchedules) {
            add(each, thesaurus);
        }
    }
}
