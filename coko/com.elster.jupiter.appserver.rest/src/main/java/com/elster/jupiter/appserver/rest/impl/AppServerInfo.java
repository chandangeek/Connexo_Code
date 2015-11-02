package com.elster.jupiter.appserver.rest.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.ImportScheduleOnAppServer;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.streams.Functions;

import java.util.List;
import java.util.stream.Collectors;

public class AppServerInfo {

    public String name;
    public boolean active;
    public List<SubscriberExecutionSpecInfo> executionSpecs;
    public List<ImportScheduleInfo> importServices;
    public String importDirectory;
    public String exportDirectory;
    public long version;

    public AppServerInfo() {
    }

    public static AppServerInfo of(AppServer appServer, String importPath, String exportPath, Thesaurus thesaurus) {
        return new AppServerInfo(appServer, importPath, exportPath, thesaurus);
    }

    public AppServerInfo(AppServer appServer, String importPath, String exportPath, Thesaurus thesaurus) {
        name = appServer.getName();
        active = appServer.isActive();
        version = appServer.getVersion();
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
        importDirectory = importPath;
        exportDirectory = exportPath;
    }

}
