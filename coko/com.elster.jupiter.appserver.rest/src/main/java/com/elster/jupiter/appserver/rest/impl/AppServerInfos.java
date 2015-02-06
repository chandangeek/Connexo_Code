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

    public AppServerInfos(Iterable<AppServer> appServers, Thesaurus thesaurus) {
        addAll(appServers, thesaurus);
    }

    public void add(AppServer appServer, Thesaurus thesaurus) {
        AppServerInfo result = AppServerInfo.of(appServer, thesaurus);
        appServers.add(result);
        total++;
    }

    public void addAll(Iterable<AppServer> appServers, Thesaurus thesaurus) {
        for (AppServer each : appServers) {
            add(each, thesaurus);
        }
    }
}



