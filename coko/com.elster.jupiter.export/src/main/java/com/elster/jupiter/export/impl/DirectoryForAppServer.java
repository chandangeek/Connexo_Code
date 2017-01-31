/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.inject.Inject;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

class DirectoryForAppServer {

    private DataModel dataModel;

    private Reference<AppServer> appServer = ValueReference.absent();
    @ValidFileLocation(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.INVALIDCHARS_EXCEPTION + "}")
    private String pathString;

    private transient Path path;
    private transient boolean fromDb = true;

    @Inject
    DirectoryForAppServer(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    public static DirectoryForAppServer from(DataModel dataModel, AppServer appServer) {
        return dataModel.getInstance(DirectoryForAppServer.class).init(appServer);
    }

    private DirectoryForAppServer init(AppServer appServer) {
        this.appServer.set(appServer);
        this.fromDb = false;
        return this;
    }

    void setPath(Path path) {
        this.pathString = path.toString();
        this.path = path;
    }

    Optional<Path> getPath() {
        if (path == null && pathString != null) {
            path = Paths.get(pathString);
        }
        return Optional.ofNullable(path);
    }

    public void save() {
        if (fromDb) {
            dataModel.mapper(DirectoryForAppServer.class).update(this);
            return;
        }
        dataModel.mapper(DirectoryForAppServer.class).persist(this);
    }

    AppServer getAppServer() {
        return appServer.get();
    }
}
