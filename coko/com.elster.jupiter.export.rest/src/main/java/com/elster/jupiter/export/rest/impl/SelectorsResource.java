/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.DataExportWebService;
import com.elster.jupiter.export.security.Privileges;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.conditions.Where;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Path("/selectors")
public class SelectorsResource {
    private static final String SUPPORTED_OPERATION_PARAMETER_NAME = "operation";

    private final DataExportService dataExportService;
    private final PropertyValueInfoService propertyValueInfoService;
    private final EndPointConfigurationService endPointConfigurationService;

    @Inject
    public SelectorsResource(DataExportService dataExportService, PropertyValueInfoService propertyValueInfoService, EndPointConfigurationService endPointConfigurationService) {
        this.dataExportService = dataExportService;
        this.propertyValueInfoService = propertyValueInfoService;
        this.endPointConfigurationService = endPointConfigurationService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DATA_EXPORT_TASK,
            Privileges.Constants.ADMINISTRATE_DATA_EXPORT_TASK,
            Privileges.Constants.UPDATE_DATA_EXPORT_TASK,
            Privileges.Constants.UPDATE_SCHEDULE_DATA_EXPORT_TASK,
            Privileges.Constants.RUN_DATA_EXPORT_TASK})
    public PagedInfoList getAvailableSelectors(@BeanParam JsonQueryParameters queryParameters, @HeaderParam(DataExportTaskResource.X_CONNEXO_APPLICATION_NAME) String application) {
        List<SelectorInfo> selectors = dataExportService.getAvailableSelectors().stream()
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
        return PagedInfoList.fromCompleteList("selectors", selectors, queryParameters);
    }

    @GET
    @Path("/{name}/endpoints")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DATA_EXPORT_TASK,
            Privileges.Constants.ADMINISTRATE_DATA_EXPORT_TASK,
            Privileges.Constants.UPDATE_DATA_EXPORT_TASK,
            Privileges.Constants.UPDATE_SCHEDULE_DATA_EXPORT_TASK,
            Privileges.Constants.RUN_DATA_EXPORT_TASK})
    public PagedInfoList getAvailableWebServiceEndpoints(@PathParam("name") String selectorName,
                                                         @QueryParam(SUPPORTED_OPERATION_PARAMETER_NAME) DataExportWebService.Operation mode,
                                                         @BeanParam JsonQueryParameters queryParameters) {
        List<String> serviceNames = dataExportService.getDataSelectorFactory(selectorName)
                .map(dataExportService::getExportWebServicesMatching)
                .orElseGet(Collections::emptyList)
                .stream()
                .filter(service -> service.getSupportedOperations().contains(mode))
                .map(DataExportWebService::getName)
                .collect(Collectors.toList());
        List<IdWithNameInfo> endpoints = endPointConfigurationService.streamEndPointConfigurations()
                .filter(Where.where("webServiceName").in(serviceNames))
                .filter(Where.where("active").isEqualTo(true))
                .sorted(Order.ascending("upper(name)"))
                .map(IdWithNameInfo::new)
                .collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("endpoints", endpoints, queryParameters);
    }
}
