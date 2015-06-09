package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.AppServerCommand;
import com.elster.jupiter.appserver.Command;
import com.elster.jupiter.appserver.ImportScheduleOnAppServer;
import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.orm.DataModel;

import javax.inject.Inject;
import java.util.Optional;
import java.util.Properties;

public class ImportScheduleOnAppServerImpl implements ImportScheduleOnAppServer {

    private long importScheduleId;
    private transient ImportSchedule importSchedule;
    private String appServerName;
    private transient AppServer appServer;
    private DataModel dataModel;

    private final FileImportService fileImportService;

    @Inject
	ImportScheduleOnAppServerImpl(DataModel dataModel, FileImportService fileImportService) {
        this.dataModel = dataModel;
        this.fileImportService = fileImportService;
    }

    static ImportScheduleOnAppServerImpl from(DataModel dataModel, FileImportService fileImportService, ImportSchedule importSchedule, AppServer appServer) {
        ImportScheduleOnAppServerImpl importScheduleOnAppServer = new ImportScheduleOnAppServerImpl(dataModel, fileImportService);
        return importScheduleOnAppServer.init(importSchedule, appServer);
    }

    ImportScheduleOnAppServerImpl init(ImportSchedule importSchedule, AppServer appServer) {
        this.importScheduleId = importSchedule.getId();
        this.importSchedule = importSchedule;
        this.appServerName = appServer.getName();
        this.appServer = appServer;
        return this;
    }

    @Override
    public Optional<ImportSchedule> getImportSchedule() {
        if (importSchedule == null) {
            importSchedule = fileImportService.getImportSchedule(importScheduleId)
                    .orElse(null);

        }
        return Optional.ofNullable(importSchedule);
    }

    @Override
    public AppServer getAppServer() {
        if (appServer == null) {
            appServer = dataModel.mapper(AppServer.class).getOptional(appServerName).get();
        }
        return appServer;
    }

    @Override
    public void save() {
        dataModel.mapper(ImportScheduleOnAppServer.class).persist(this);
        Properties properties = new Properties();
        properties.setProperty("id", String.valueOf(importSchedule.getId()));
        getAppServer().sendCommand(new AppServerCommand(Command.FILEIMPORT_ACTIVATED, properties));
    }
}
