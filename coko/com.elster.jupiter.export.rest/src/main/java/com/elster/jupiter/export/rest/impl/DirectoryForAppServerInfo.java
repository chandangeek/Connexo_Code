/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.util.PathVerification;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.xml.bind.annotation.XmlRootElement;
import java.nio.file.Path;
import java.nio.file.Paths;

@XmlRootElement
public class DirectoryForAppServerInfo {

    public String appServerName;
    public String directory;
    public long version;

    public DirectoryForAppServerInfo() {
    }

    public DirectoryForAppServerInfo(AppServer appServer, Path path) {
        appServerName = appServer.getName();
        version = appServer.getVersion();
        PathVerification.validatePathForFolders(path.toString());
        directory = path.toString();
    }

    @JsonIgnore
    public Path path() {
        return Paths.get(directory);
    }
}
