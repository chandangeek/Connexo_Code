package com.elster.jupiter.appserver;

import com.elster.jupiter.fileimport.ImportSchedule;

public interface ImportScheduleOnAppServer {

    ImportSchedule getImportSchedule();

    AppServer getAppServer();

    void save();
}
