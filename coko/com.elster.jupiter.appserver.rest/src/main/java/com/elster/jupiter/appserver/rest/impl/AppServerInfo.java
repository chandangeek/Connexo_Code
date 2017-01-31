/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.appserver.rest.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.ImportScheduleOnAppServer;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.util.streams.Functions;

import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.stream.Collectors;

public class AppServerInfo {

    public String name;
    public boolean active;
    public List<SubscriberExecutionSpecInfo> executionSpecs;
    public List<ImportScheduleInfo> importServices;
    public List<EndPointConfigurationInfo> endPointConfigurations;
    public String importDirectory;
    public String exportDirectory;
    public long version;

    public AppServerInfo() {
    }

    public static AppServerInfo of(AppServer appServer, String importPath, String exportPath, Thesaurus thesaurus, WebServicesService webServicesService, UriInfo uriInfo) {
        return new AppServerInfo(appServer, importPath, exportPath, thesaurus, webServicesService, uriInfo);
    }

    public AppServerInfo(AppServer appServer, String importPath, String exportPath, Thesaurus thesaurus, WebServicesService webServicesService, UriInfo uriInfo) {
        EndPointConfigurationInfoFactory endPointConfigurationInfoFactory = new EndPointConfigurationInfoFactory(thesaurus, webServicesService);
        name = appServer.getName();
        active = appServer.isActive();
        version = appServer.getVersion();
        executionSpecs = appServer.getSubscriberExecutionSpecs().stream()
                .map(spec -> SubscriberExecutionSpecInfo.of(spec))
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
        endPointConfigurations = appServer.supportedEndPoints()
                .stream()
                .filter(EndPointConfiguration::isInbound)
                .map(epc -> endPointConfigurationInfoFactory.from(epc, uriInfo))
                .collect(Collectors.toList());
    }

}
