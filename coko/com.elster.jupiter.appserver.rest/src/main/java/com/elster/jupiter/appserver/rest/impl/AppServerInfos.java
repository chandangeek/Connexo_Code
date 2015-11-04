package com.elster.jupiter.appserver.rest.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.nls.Thesaurus;

import java.util.ArrayList;
import java.util.List;


public class AppServerInfos {

    public int total;
    public List<AppServerInfo> appServers = new ArrayList<>();

    public AppServerInfos() {
    }

    public void add(AppServer appServer, String importPath, String exportPath, Thesaurus thesaurus) {
        AppServerInfo result = AppServerInfo.of(appServer, importPath, exportPath, thesaurus);
        appServers.add(result);
        total++;
    }

}



