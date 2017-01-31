/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.export.security.Privileges;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.util.conditions.Order;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.stream.Collectors;

@Path("/fields")
public class FieldResource {

    private final MeteringGroupsService meteringGroupsService;
    private final RestQueryService restQueryService;

    @Inject
    public FieldResource(MeteringGroupsService meteringGroupsService, RestQueryService restQueryService) {
        this.meteringGroupsService = meteringGroupsService;
        this.restQueryService = restQueryService;
    }

    @Path("/metergroups")
    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.VIEW_DATA_EXPORT_TASK, Privileges.Constants.ADMINISTRATE_DATA_EXPORT_TASK, Privileges.Constants.UPDATE_DATA_EXPORT_TASK, Privileges.Constants.UPDATE_SCHEDULE_DATA_EXPORT_TASK, Privileges.Constants.RUN_DATA_EXPORT_TASK})
    public PagedInfoList getDeviceGroups(@Context UriInfo uriInfo, @BeanParam JsonQueryParameters parameters) {
        QueryParameters queryParameters = QueryParameters.wrap(uriInfo.getQueryParameters());
        List<EndDeviceGroup> allDeviceGroups = queryEndDeviceGroups(queryParameters);
        return PagedInfoList.fromPagedList("metergroups",
                allDeviceGroups.stream()
                        .map(endDeviceGroup -> new IdWithNameInfo(endDeviceGroup.getId(), endDeviceGroup.getName()))
                        .collect(Collectors.toList()), parameters);
    }

    private List<EndDeviceGroup> queryEndDeviceGroups(QueryParameters queryParameters) {
        Query<EndDeviceGroup> query = meteringGroupsService.getEndDeviceGroupQuery();
        RestQuery<EndDeviceGroup> restQuery = restQueryService.wrap(query);
        return restQuery.select(queryParameters, Order.ascending("upper(name)"));
    }

    @Path("/usagepointgroups")
    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.VIEW_DATA_EXPORT_TASK, Privileges.Constants.ADMINISTRATE_DATA_EXPORT_TASK, Privileges.Constants.UPDATE_DATA_EXPORT_TASK, Privileges.Constants.UPDATE_SCHEDULE_DATA_EXPORT_TASK, Privileges.Constants.RUN_DATA_EXPORT_TASK})
    public PagedInfoList getUsagePointGroups(@Context UriInfo uriInfo, @BeanParam JsonQueryParameters parameters) {
        QueryParameters queryParameters = QueryParameters.wrap(uriInfo.getQueryParameters());
        List<UsagePointGroup> allDeviceGroups = queryUsagePointGroups(queryParameters);
        return PagedInfoList.fromPagedList("usagePointGroups",
                allDeviceGroups.stream()
                        .map(usagePointGroup -> new IdWithNameInfo(usagePointGroup.getId(), usagePointGroup.getName()))
                        .collect(Collectors.toList()), parameters);
    }

    private List<UsagePointGroup> queryUsagePointGroups(QueryParameters queryParameters) {
        Query<UsagePointGroup> query = meteringGroupsService.getUsagePointGroupQuery();
        RestQuery<UsagePointGroup> restQuery = restQueryService.wrap(query);
        return restQuery.select(queryParameters, Order.ascending("upper(name)"));
    }
}
