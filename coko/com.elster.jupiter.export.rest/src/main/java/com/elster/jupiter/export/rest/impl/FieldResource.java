/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.DataSelectorConfig;
import com.elster.jupiter.export.ExportTask;
import com.elster.jupiter.export.security.Privileges;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.orm.QueryStream;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.conditions.Where;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.elster.jupiter.export.rest.impl.DataExportTaskResource.X_CONNEXO_APPLICATION_NAME;

@Path("/fields")
public class FieldResource {
    private final MeteringGroupsService meteringGroupsService;
    private final RestQueryService restQueryService;
    private final DataExportService dataExportService;

    @Inject
    public FieldResource(MeteringGroupsService meteringGroupsService, RestQueryService restQueryService, DataExportService dataExportService) {
        this.meteringGroupsService = meteringGroupsService;
        this.restQueryService = restQueryService;
        this.dataExportService = dataExportService;
    }

    @Path("/metergroups")
    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.VIEW_DATA_EXPORT_TASK,
            Privileges.Constants.ADMINISTRATE_DATA_EXPORT_TASK,
            Privileges.Constants.UPDATE_DATA_EXPORT_TASK,
            Privileges.Constants.UPDATE_SCHEDULE_DATA_EXPORT_TASK,
            Privileges.Constants.RUN_DATA_EXPORT_TASK})
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
    @RolesAllowed({Privileges.Constants.VIEW_DATA_EXPORT_TASK,
            Privileges.Constants.ADMINISTRATE_DATA_EXPORT_TASK,
            Privileges.Constants.UPDATE_DATA_EXPORT_TASK,
            Privileges.Constants.UPDATE_SCHEDULE_DATA_EXPORT_TASK,
            Privileges.Constants.RUN_DATA_EXPORT_TASK})
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

    @Path("/taskstopair")
    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DATA_EXPORT_TASK,
            Privileges.Constants.ADMINISTRATE_DATA_EXPORT_TASK,
            Privileges.Constants.UPDATE_DATA_EXPORT_TASK,
            Privileges.Constants.UPDATE_SCHEDULE_DATA_EXPORT_TASK,
            Privileges.Constants.RUN_DATA_EXPORT_TASK})
    public PagedInfoList getTasksToPair(@QueryParam("id") Long id,
                                        @BeanParam JsonQueryParameters parameters,
                                        @HeaderParam(X_CONNEXO_APPLICATION_NAME) String appCode) {
        ExportTask exportTask = id == null ? null : dataExportService.findExportTask(id).orElse(null);
        List<IdWithNameInfo> result = getTasksToPair(exportTask, parameters, DataExportTaskResource.getApplicationNameFromCode(appCode)).stream()
                .map(IdWithNameInfo::new)
                .collect(Collectors.toList());
        return PagedInfoList.fromPagedList("tasksToPair", result, parameters);
    }

    private List<? extends ExportTask> getTasksToPair(ExportTask exportTask, JsonQueryParameters parameters, String application) {
        if (exportTask != null && !exportTask.getReadingDataSelectorConfig().isPresent()) {
            return Collections.emptyList();
        }
        Condition currentPairCondition = Optional.ofNullable(exportTask)
                .flatMap(ExportTask::getPairedTask)
                .map(ExportTask::getId)
                .map(Where.where("id")::isEqualTo)
                .orElse(Condition.FALSE);
        QueryStream<? extends ExportTask> stream = dataExportService.streamExportTasks()
                .join(DataSelectorConfig.class)
                .filter(Where.where("pairedTask").isNull().or(currentPairCondition))
                .filter(Where.where("dataSelectorConfig.class").in("MeterReadingSelectorConfig", "UsagePointReadingSelectorConfig"))
                .filter(Where.where("recurrentTask.application").isEqualTo(application));
        if (exportTask != null) {
            stream = stream.filter(Where.where("id").isNotEqual(exportTask.getId()))
                    .filter(Where.where("dataSelector").isEqualTo(exportTask.getDataSelectorFactory().getName()));
        }
        stream = parameters.apply(stream);
        return stream.sorted(Order.ascending("recurrentTask.name").toLowerCase())
                .select();
    }
}
