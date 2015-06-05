package com.elster.jupiter.appserver.rest.impl;

import com.elster.jupiter.fileimport.ImportSchedule;

public class ImportScheduleInfo {
    public long id;
    public String name;
    public boolean active;


    ImportScheduleInfo(){
    }

    private ImportScheduleInfo(ImportSchedule importSchedule) {
        this.id = importSchedule.getId();
        this.name = importSchedule.getName();
        this.active = importSchedule.isActive();
    }

    public static ImportScheduleInfo of(ImportSchedule importSchedule) {
        return new ImportScheduleInfo(importSchedule);
    }

    public String getName() {
        return this.name;
    }
}
