package com.elster.jupiter.appserver.rest.impl;

import com.elster.jupiter.appserver.AppServer;

import java.util.List;
import java.util.stream.Collectors;

public class AppServerInfo {

    public String name;
    public boolean active;
    public List<SubscriberExecutionSpecInfo> executionSpecs;

    public AppServerInfo() {}

    public static AppServerInfo of(AppServer appServer) {
        return new AppServerInfo(appServer);
    }

    public AppServerInfo(AppServer appServer) {
        name = appServer.getName();
        active = appServer.isActive();
        executionSpecs = appServer.getSubscriberExecutionSpecs().stream()
                .map(SubscriberExecutionSpecInfo::of)
                .collect(Collectors.toList());
    }

    public static List<AppServerInfo> from(List<AppServer> appServers) {
        return appServers.stream()
                .map(AppServerInfo::of)
                .collect(Collectors.toList());
    }

}
