package com.elster.jupiter.appserver.rest.impl;

import com.elster.jupiter.appserver.AppServer;

import java.util.ArrayList;
import java.util.List;


public class AppServerInfos {

    public int total;
    public List<AppServerInfo> appServers = new ArrayList<>();

    public AppServerInfos() {
    }

    public AppServerInfos(Iterable<AppServer> appServers) {
        addAll(appServers);
    }

    public void add(AppServer appServer) {
        AppServerInfo result = AppServerInfo.of(appServer);
        appServers.add(result);
        total++;
    }

    public void addAll(Iterable<AppServer> appServers) {
        for (AppServer each : appServers) {
            add(each);
        }
    }
}



