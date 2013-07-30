package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.AppServerCommand;
import com.elster.jupiter.appserver.Command;
import com.elster.jupiter.appserver.ImportScheduleOnAppServer;
import com.elster.jupiter.fileimport.ImportSchedule;

import java.util.Properties;

public class ImportScheduleOnAppServerImpl implements ImportScheduleOnAppServer {

    private long importScheduleId;
    private transient ImportSchedule importSchedule;
    private String appServerName;
    private transient AppServer appServer;

    private ImportScheduleOnAppServerImpl() {
    }

    public ImportScheduleOnAppServerImpl(ImportSchedule importSchedule, AppServer appServer) {
        this.importScheduleId = importSchedule.getId();
        this.importSchedule = importSchedule;
        this.appServerName = appServer.getName();
        this.appServer = appServer;
    }

    @Override
    public ImportSchedule getImportSchedule() {
        if (importSchedule == null) {
            importSchedule = Bus.getFileImportService().getImportSchedule(importScheduleId);
        }
        return importSchedule;
    }

    @Override
    public AppServer getAppServer() {
        if (appServer == null) {
            appServer = Bus.getOrmClient().getAppServerFactory().get(appServerName).get();
        }
        return appServer;
    }

    @Override
    public void save() {
        Bus.getOrmClient().getImportScheduleOnAppServerFactory().persist(this);
        Properties properties = new Properties();
        properties.setProperty("id", String.valueOf(importSchedule.getId()));
        getAppServer().sendCommand(new AppServerCommand(Command.FILEIMPORT_ACTIVATED, properties));
    }
}
