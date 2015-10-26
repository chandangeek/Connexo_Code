package com.elster.jupiter.appserver.rest.impl;

import com.elster.jupiter.appserver.AppServer;

import java.nio.file.Path;

public class DirectoryForAppServerInfo {

    public String appServerName;

    public String directory;

    public DirectoryForAppServerInfo() {
    }

    public DirectoryForAppServerInfo(AppServer appServer, Path path) {
        appServerName = appServer.getName();
        directory = path.toString();
    }
}