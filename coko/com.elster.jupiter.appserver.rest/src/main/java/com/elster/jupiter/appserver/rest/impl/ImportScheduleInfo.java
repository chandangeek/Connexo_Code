package com.elster.jupiter.appserver.rest.impl;


import com.elster.jupiter.appserver.ImportScheduleOnAppServer;

import com.elster.jupiter.nls.Thesaurus;
//import com.elster.jupiter.rest.util.properties.PropertyInfo;
//import com.elster.jupiter.time.rest.PeriodicalExpressionInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mandr on 5/25/2015.
 */
public class ImportScheduleInfo {

    public long id;
    public String displayName;
    public Boolean active;
    public String destinationName;
    public String importDirectory;
    public String inProcessDirectory;
    public String successDirectory;
    public String failureDirectory;
    public String pathMatcher;
    public String importerName;
    public Integer scanFrequency;
    public String application;
    //public FileImporterInfo importerInfo;
    //public PeriodicalExpressionInfo schedule;
    //public List<PropertyInfo> properties = new ArrayList<>();

    public ImportScheduleInfo() {
    }

    public ImportScheduleInfo(ImportScheduleOnAppServer importScheduleOnAppServer, Thesaurus thesaurus) {
        //id = importScheduleOnAppServer.getImportSchedule().getId();
        id = 1L;
        //active = importScheduleOnAppServer.isActive();
        //name = importScheduleOnAppServer.getImportSchedule().getName();
        displayName = "test";
        //destinationName = importScheduleOnAppServer.getDestination().getName();
    }
    public ImportScheduleInfo(long nId)
    {
        id = nId;
        displayName = "Import" + nId;
    }
    /*
    public static ImportScheduleInfo of(ImportScheduleOnAppServer importScheduleOnAppServer, Thesaurus thesaurus) {
        return of(importScheduleOnAppServer.getImportSchedule(), thesaurus);
    }
    */

}
