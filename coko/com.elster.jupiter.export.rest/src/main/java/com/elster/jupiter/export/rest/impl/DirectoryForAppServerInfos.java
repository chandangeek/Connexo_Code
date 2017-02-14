/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.appserver.AppServer;

import javax.xml.bind.annotation.XmlRootElement;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@XmlRootElement
public class DirectoryForAppServerInfos {

	public int total;
	public List<DirectoryForAppServerInfo> directories = new ArrayList<>();

	public DirectoryForAppServerInfos() {
    }

    public DirectoryForAppServerInfos(Map<AppServer, Path> mappings) {
	    mappings.entrySet().stream()
            .forEach(entry -> add(entry.getKey(), entry.getValue()));
	}

    public DirectoryForAppServerInfo add(AppServer appServer, Path path) {
        DirectoryForAppServerInfo result = new DirectoryForAppServerInfo(appServer, path);
        directories.add(result);
	    total++;
	    return result;
	}
}


