/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.appserver.rest.impl;

import com.elster.jupiter.fileimport.ImportSchedule;

import java.util.ArrayList;
import java.util.List;

public class ImportScheduleInfos {
    public int total;
    public List<ImportScheduleInfo> importServices = new ArrayList<>();

    ImportScheduleInfos(){
    }

    public ImportScheduleInfos(Iterable<ImportSchedule> importSchedules) {
        addAll(importSchedules);
    }

    public void add(ImportSchedule importSchedule) {
        ImportScheduleInfo result = ImportScheduleInfo.of(importSchedule);
        importServices.add(result);
        total++;
    }

    public void addAll(Iterable<ImportSchedule> importSchedules) {
        for (ImportSchedule importSchedule : importSchedules) {
            add(importSchedule);
        }
    }
}
