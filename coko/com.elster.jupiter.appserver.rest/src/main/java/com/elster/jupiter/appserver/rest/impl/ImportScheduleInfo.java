package com.elster.jupiter.appserver.rest.impl;

import com.elster.jupiter.fileimport.ImportSchedule;

public class ImportScheduleInfo {
    public long id;
    public String name;
    public boolean deleted;

    ImportScheduleInfo(){
    }

    private ImportScheduleInfo(ImportSchedule importSchedule) {
        this.id = importSchedule.getId();
        this.name = importSchedule.getName();
        this.deleted = importSchedule.getObsoleteTime()!=null;
    }

    public static ImportScheduleInfo of(ImportSchedule importSchedule) {
        return new ImportScheduleInfo(importSchedule);
    }

    public String getName() {
        return this.name;
    }
}
