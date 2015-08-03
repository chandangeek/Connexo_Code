package com.elster.jupiter.appserver.rest.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.ImportScheduleOnAppServer;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.streams.Functions;

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class AppServerInfo {

    public String name;
    public boolean active;
    public List<SubscriberExecutionSpecInfo> executionSpecs;
    public List<ImportScheduleInfo> importServices;

    public AppServerInfo() {}

    public static AppServerInfo of(AppServer appServer, Thesaurus thesaurus) {
        return new AppServerInfo(appServer, thesaurus);
    }

    public AppServerInfo(AppServer appServer, Thesaurus thesaurus) {
        name = appServer.getName();
        active = appServer.isActive();
        executionSpecs = appServer.getSubscriberExecutionSpecs().stream()
                .map(spec -> SubscriberExecutionSpecInfo.of(spec, thesaurus))
                .collect(Collectors.toList());
        importServices = appServer.getImportSchedulesOnAppServer()
                .stream()
                .map(ImportScheduleOnAppServer::getImportSchedule)
                .flatMap(Functions.asStream())
                .map(ImportScheduleInfo::of)
                .filter(s -> !s.deleted)
                .collect(Collectors.toList());
    }

    public static List<AppServerInfo> from(List<AppServer> appServers, Thesaurus thesaurus) {
        return appServers.stream()
                .map(appServer -> AppServerInfo.of(appServer, thesaurus))
                .collect(Collectors.toList());
    }

}
