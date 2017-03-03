/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fileimport.rest.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.fileimport.FileImportService;
import com.elster.jupiter.fileimport.ImportSchedule;
import com.elster.jupiter.fileimport.security.Privileges;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.stream.Collectors;

@Path("/fields")
public class FieldResource {

    private final FileImportService fileImportService;
    private final FileImportScheduleInfoFactory fileImportScheduleInfoFactory;
    private final AppService appService;

    @Inject
    public FieldResource(FileImportService fileImportService, FileImportScheduleInfoFactory fileImportScheduleInfoFactory, AppService appService) {
        this.fileImportService = fileImportService;
        this.fileImportScheduleInfoFactory = fileImportScheduleInfoFactory;
        this.appService = appService;
    }

    @GET
    @Path("/fileupload")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_IMPORT_SERVICES, Privileges.Constants.VIEW_IMPORT_SERVICES, Privileges.Constants.IMPORT_FILE})
    public PagedInfoList getImporters(@HeaderParam("X-CONNEXO-APPLICATION-NAME") String applicationName, @BeanParam JsonQueryParameters queryParameters) {
        List<FileImportScheduleInfo> availableImportSchedules = appService.findAppServers()
                .stream()
                .filter(AppServer::isActive)
                .flatMap(server -> server.getImportSchedulesOnAppServer()
                                .stream()
                                .map(schedule -> schedule.getImportSchedule().orElse(null))
                                .filter(importSchedule -> importSchedule.getApplicationName().equals(applicationName))
                                .filter(ImportSchedule::activeInUI)
                                .collect(Collectors.toList()).stream())
                .map(fileImportScheduleInfoFactory::asInfo)
                .collect(Collectors.toList());

        return PagedInfoList.fromPagedList("data", availableImportSchedules, queryParameters);
    }
}
