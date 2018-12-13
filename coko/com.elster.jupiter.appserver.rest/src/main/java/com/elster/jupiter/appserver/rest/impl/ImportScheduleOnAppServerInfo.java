/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.appserver.rest.impl;

/**
 * Created by mandr on 5/25/2015.
 */

public class ImportScheduleOnAppServerInfo {

    public ImportScheduleInfo importSchedule;
    public int numberOfThreads;

    public ImportScheduleOnAppServerInfo() {
    }

    private ImportScheduleOnAppServerInfo(ImportScheduleInfo importScheduleInfo, int numberOfThreads) {
        this.importSchedule = importScheduleInfo;
        this.numberOfThreads = numberOfThreads;
    }

    /*
    public ImportScheduleOnAppServerInfo(long nID) {
        this.importSchedule = new ImportScheduleInfo(nID);
        this.numberOfThreads = (int)nID;
    }
    */
    /*
    public static ImportScheduleOnAppServerInfo of(ImportScheduleOnAppServer importScheduleOnAppServer, Thesaurus thesaurus) {
        return new ImportScheduleOnAppServerInfo(ImportScheduleInfo.of(importScheduleOnAppServer, thesaurus), executionSpec.getThreadCount());
    }
    */
}
