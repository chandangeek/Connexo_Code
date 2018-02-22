/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.dataquality.rest.impl;

import com.elster.jupiter.dataquality.DataQualityKpi;
import com.elster.jupiter.dataquality.DataQualityKpiService;
import com.elster.jupiter.dataquality.UsagePointDataQualityKpi;
import com.elster.jupiter.dataquality.security.Privileges;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.ListPager;
import com.elster.jupiter.rest.util.LongIdWithNameInfo;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.RestValidationBuilder;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskService;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.elster.jupiter.dataquality.rest.impl.DataQualityKpiInfo.UsagePointDataQualityKpiInfo;

@Path("/usagePointKpis")
public class UsagePointKpiResource {

    private final DataQualityKpiService dataQualityKpiService;
    private final TaskService taskService;
    private final DataQualityKpiInfoFactory dataQualityKpiInfoFactory;
    private final ResourceHelper resourceHelper;

    @Inject
    public UsagePointKpiResource(DataQualityKpiService dataQualityKpiService, DataQualityKpiInfoFactory dataQualityKpiInfoFactory, TaskService taskService, ResourceHelper resourceHelper) {
        this.dataQualityKpiService = dataQualityKpiService;
        this.dataQualityKpiInfoFactory = dataQualityKpiInfoFactory;
        this.taskService = taskService;
        this.resourceHelper = resourceHelper;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_DATA_QUALITY_KPI_CONFIGURATION, Privileges.Constants.VIEW_DATA_QUALITY_KPI_CONFIGURATION})
    public PagedInfoList getAllDataQualityKpis(@BeanParam JsonQueryParameters queryParameters) {
        List<UsagePointDataQualityKpi> allKpis = dataQualityKpiService.usagePointDataQualityKpiFinder().find();
        List<UsagePointDataQualityKpi> pagedKpis = ListPager.of(allKpis, usagePointKpiComparator()).from(queryParameters).find();
        List<UsagePointDataQualityKpiInfo> infos = pagedKpis.stream()
                .map(dataQualityKpiInfoFactory::from)
                .collect(Collectors.toList());
        return PagedInfoList.fromPagedList("usagePointKpis", infos, queryParameters);
    }

    private Comparator<UsagePointDataQualityKpi> usagePointKpiComparator() {
        return Comparator.<UsagePointDataQualityKpi, String>comparing(kpi -> kpi.getUsagePointGroup().getName().toUpperCase())
                .thenComparing(kpi -> kpi.getMetrologyPurpose().getName().toUpperCase());
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_DATA_QUALITY_KPI_CONFIGURATION, Privileges.Constants.VIEW_DATA_QUALITY_KPI_CONFIGURATION})
    public UsagePointDataQualityKpiInfo getDataQualityKpiById(@PathParam("id") long id) {
        UsagePointDataQualityKpi dataQualityKpi = dataQualityKpiService.findUsagePointDataQualityKpi(id)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        return dataQualityKpiInfoFactory.from(dataQualityKpi);
    }

    @GET
    @Path("/recurrenttask/{recurrenttaskId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_DATA_QUALITY_KPI_CONFIGURATION, Privileges.Constants.VIEW_DATA_QUALITY_KPI_CONFIGURATION})
    public UsagePointDataQualityKpiInfo getDataQualityKpiByRecurrentTaskId(@PathParam("recurrenttaskId") long id) {
        UsagePointDataQualityKpi dataQualityKpi = dataQualityKpiService.findUsagePointDataQualityKpiByRecurrentTaskId(id)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        return dataQualityKpiInfoFactory.from(dataQualityKpi);
    }

    @POST
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_DATA_QUALITY_KPI_CONFIGURATION})
    public Response createDataQualityKpi(UsagePointDataQualityKpiInfo info) {
        new RestValidationBuilder()
                .notEmpty(info.usagePointGroup, "usagePointGroup", MessageSeeds.CAN_NOT_BE_EMPTY)
                .on(info.purposes).field("purposes").check(purposes -> purposes != null && purposes.length != 0).message(MessageSeeds.CAN_NOT_BE_EMPTY).test()
                .notEmpty(info.frequency, "frequency", MessageSeeds.CAN_NOT_BE_EMPTY)
                .validate();
        for (LongIdWithNameInfo purpose : info.purposes) {
            info.metrologyPurpose = purpose;
            dataQualityKpiInfoFactory.createNewKpi(info);
        }
        return Response.status(Response.Status.CREATED).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_DATA_QUALITY_KPI_CONFIGURATION})
    public Response deleteDataQualityKpi(@PathParam("id") long id, UsagePointDataQualityKpiInfo info) {
        info.id = id;
        resourceHelper.findAndLockDataQualityKpi(info).delete();
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_DATA_QUALITY_KPI_CONFIGURATION})
    public Response editDataQualityKpi(@PathParam("id") long id, UsagePointDataQualityKpiInfo info) {
        info.id = id;
        DataQualityKpi qualityKpi = resourceHelper.findAndLockDataQualityKpi(info);
        qualityKpi.setNextRecurrentTasks(this.findRecurrentTaskOrThrowException(info.nextRecurrentTasks));
        qualityKpi.save();
        return Response.status(Response.Status.OK).build();
    }

    private List<RecurrentTask> findRecurrentTaskOrThrowException(List<TaskInfo> nextRecurrentTasks) {
        List<RecurrentTask> recurrentTasks = new ArrayList<>();
        if (nextRecurrentTasks != null) {
            nextRecurrentTasks.forEach(taskInfo -> {
                recurrentTasks.add(taskService.getRecurrentTask(taskInfo.id)
                        .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND)));

            });
        }
        return recurrentTasks;
    }
}
