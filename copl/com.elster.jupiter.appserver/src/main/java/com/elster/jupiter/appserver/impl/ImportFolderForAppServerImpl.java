package com.elster.jupiter.appserver.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.ImportFolderForAppServer;
import com.elster.jupiter.orm.DataModel;

import javax.inject.Inject;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class ImportFolderForAppServerImpl implements ImportFolderForAppServer {

    private Path importFolderPath;
    private String appServerName;
    private transient AppServer appServer;
    private DataModel dataModel;
    private transient boolean fromDb = true;


    @Inject
    ImportFolderForAppServerImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    static ImportFolderForAppServerImpl from(DataModel dataModel, Path importFolderPath, AppServer appServer) {
        ImportFolderForAppServerImpl importScheduleOnAppServer = new ImportFolderForAppServerImpl(dataModel);
        return importScheduleOnAppServer.init(importFolderPath, appServer);
    }

    ImportFolderForAppServerImpl init(Path importFolderPath, AppServer appServer) {
        this.importFolderPath = importFolderPath;
        this.appServerName = appServer.getName();
        this.appServer = appServer;
        this.fromDb = false;
        return this;
    }


    @Override
    public  Optional<Path>  getImportFolder() {
        return Optional.ofNullable(importFolderPath);
    }

    @Override
    public AppServer getAppServer() {
        if (appServer == null) {
            appServer = dataModel.mapper(AppServer.class).getOptional(appServerName).get();
        }
        return appServer;
    }

    @Override
    public void setImportFolder(Path path) {
        this.importFolderPath = path;
    }

    @Override
    public void save() {
        if(fromDb)  {
            dataModel.mapper(ImportFolderForAppServer.class).update(this);
            return;
        }
        dataModel.mapper(ImportFolderForAppServer.class).persist(this);
    }

    @Override
    public void delete() {
        dataModel.mapper(ImportFolderForAppServer.class).remove(this);
    }

}
