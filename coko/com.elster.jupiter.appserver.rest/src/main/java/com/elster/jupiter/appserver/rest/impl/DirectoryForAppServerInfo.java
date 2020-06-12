/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.appserver.rest.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.util.PathVerification;

import java.nio.file.Path;

public class DirectoryForAppServerInfo {

    public String appServerName;

    public String directory;

    public DirectoryForAppServerInfo() {
    }

    public DirectoryForAppServerInfo(AppServer appServer, Path path) {
        appServerName = appServer.getName();
        PathVerification.validatePathForFolders(path.toString());
        directory = path.toString();
    }
}