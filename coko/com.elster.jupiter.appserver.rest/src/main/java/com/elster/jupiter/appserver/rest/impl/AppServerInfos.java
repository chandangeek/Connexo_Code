package com.elster.jupiter.appserver.rest.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;

import java.util.ArrayList;
import java.util.List;


public class AppServerInfos {

    public int total;
    public List<AppServerInfo> appServers = new ArrayList<>();

    public AppServerInfos() {
    }

    public void add(AppServer appServer, String importPath, String exportPath, Thesaurus thesaurus, WebServicesService webServicesService) {
        AppServerInfo result = AppServerInfo.of(appServer, importPath, exportPath, thesaurus, webServicesService);
        appServers.add(result);
        total++;
    }

}



