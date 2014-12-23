package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.appserver.AppServer;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.xml.bind.annotation.XmlRootElement;
import java.nio.file.Path;
import java.nio.file.Paths;

@XmlRootElement
public class DirectoryForAppServerInfo {

    public String appServerName;

    public String directory;

    public DirectoryForAppServerInfo() {
    }

    public DirectoryForAppServerInfo(AppServer appServer, Path path) {
        appServerName = appServer.getName();
        directory = path.toString();
    }

    @JsonIgnore
    public Path path() {
        return Paths.get(directory);
    }
}
