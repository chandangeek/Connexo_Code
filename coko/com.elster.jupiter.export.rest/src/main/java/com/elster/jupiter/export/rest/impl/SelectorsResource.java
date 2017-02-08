/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.security.Privileges;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.stream.Collectors;

@Path("/selectors")
public class SelectorsResource {

    private final DataExportService dataExportService;
    private final PropertyValueInfoService propertyValueInfoService;

    @Inject
    public SelectorsResource(DataExportService dataExportService, PropertyValueInfoService propertyValueInfoService) {
        this.dataExportService = dataExportService;
        this.propertyValueInfoService = propertyValueInfoService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DATA_EXPORT_TASK, Privileges.Constants.ADMINISTRATE_DATA_EXPORT_TASK, Privileges.Constants.UPDATE_DATA_EXPORT_TASK, Privileges.Constants.UPDATE_SCHEDULE_DATA_EXPORT_TASK, Privileges.Constants.RUN_DATA_EXPORT_TASK})
    public PagedInfoList getAvailableSelectors(@Context UriInfo uriInfo, @BeanParam JsonQueryParameters queryParameters, @HeaderParam(DataExportTaskResource.X_CONNEXO_APPLICATION_NAME) String application) {
        List<SelectorInfo> infos = dataExportService.getAvailableSelectors().stream()
                .filter(factory -> factory.targetApplications().contains(application))
                .map(factory ->
                        new SelectorInfo(
                                factory.getName(),
                                factory.getDisplayName(),
                                propertyValueInfoService.getPropertyInfos(factory.getPropertySpecs()),
                                SelectorType.forSelector(factory.getName())
                        )
                )
                .collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("selectors", infos, queryParameters);
    }
}
