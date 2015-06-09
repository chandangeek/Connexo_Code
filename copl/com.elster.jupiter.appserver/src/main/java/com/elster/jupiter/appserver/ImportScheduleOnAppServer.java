package com.elster.jupiter.appserver;

import com.elster.jupiter.fileimport.ImportSchedule;

import java.util.Optional;

public interface ImportScheduleOnAppServer {

    Optional<ImportSchedule> getImportSchedule();

    AppServer getAppServer();

    void save();
}
