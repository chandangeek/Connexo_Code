package com.elster.jupiter.appserver.rest.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.nls.Thesaurus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by igh on 11/12/2014.
 */
public class AppServerInfo {

    public String name;

    public AppServerInfo() {}

    public static AppServerInfo from(AppServer appServer) {
        AppServerInfo appServerInfo = new AppServerInfo();
        appServerInfo.name = appServer.getName();
        return appServerInfo;
    }

    public AppServerInfo(AppServer appServer) {
        name = appServer.getName();
    }

    public static List<AppServerInfo> from(List<AppServer> appServers) {
        List<AppServerInfo> appServerInfos = new ArrayList<>();
        for (AppServer appServer : appServers) {
            appServerInfos.add(AppServerInfo.from(appServer));
        }
        return appServerInfos;
    }

}
