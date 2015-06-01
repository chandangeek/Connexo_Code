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

    public FileImportScheduleInfos(List<? extends ImportSchedule> importSchedules, Thesaurus thesaurus, PropertyUtils propertyUtils) {
        addAll(importSchedules,thesaurus, propertyUtils);
    }
    private FileImportScheduleInfo add(ImportSchedule importSchedule, Thesaurus thesaurus, PropertyUtils propertyUtils) {
        FileImportScheduleInfo result = new FileImportScheduleInfo(importSchedule, thesaurus, propertyUtils);
        importSchedules.add(result);
        total++;
        return result;
    }

    public FileImportScheduleInfos() {
    }


    private void addAll(Iterable<? extends ImportSchedule> importSchedules, Thesaurus thesaurus, PropertyUtils propertyUtils) {
        for (ImportSchedule each : importSchedules) {
            add(each, thesaurus, propertyUtils);
        }
    }
}
