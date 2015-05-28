package com.elster.jupiter.appserver.rest.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.nls.Thesaurus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AppServerInfo {

    public String name;
    public boolean active;
    public List<SubscriberExecutionSpecInfo> executionSpecs;
    public List<ImportScheduleOnAppServerInfo> importSchedules;
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

        importSchedules = new ArrayList<>();
        for(int i = 0; i<5;i++) {
            importSchedules.add(new ImportScheduleOnAppServerInfo(i));
        }
        /*
        importServices = new ArrayList<>();
        for(int i = 0; i<5;i++) {
            ImportScheduleInfo sched = new ImportScheduleInfo(i);
            importServices.add(sched);
        }*/

    }

    public static List<AppServerInfo> from(List<AppServer> appServers, Thesaurus thesaurus) {
        return appServers.stream()
                .map(appServer -> AppServerInfo.of(appServer, thesaurus))
                .collect(Collectors.toList());
    }

}
