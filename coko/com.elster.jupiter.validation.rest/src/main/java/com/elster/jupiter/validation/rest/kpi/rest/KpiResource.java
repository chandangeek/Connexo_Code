/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.rest.kpi.rest;


import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.validation.kpi.DataValidationKpi;
import com.elster.jupiter.validation.kpi.DataValidationKpiService;
import com.elster.jupiter.validation.rest.impl.MessageSeeds;
import com.elster.jupiter.validation.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Path("/kpis")
public class KpiResource {

    private final DataValidationKpiService dataValidationKpiService;
    private final DataValidationKpiInfoFactory dataValidationKpiInfoFactory;
    private final ExceptionFactory exceptionFactory;
    private final MeteringGroupsService meteringGroupsService;
    private final ConcurrentModificationExceptionFactory conflictFactory;

    @Inject
    public KpiResource(DataValidationKpiService dataValidationKpiService, ExceptionFactory exceptionFactory, MeteringGroupsService meteringGroupsService,
                       DataValidationKpiInfoFactory dataValidationKpiInfoFactory, ConcurrentModificationExceptionFactory conflictFactory) {
        this.dataValidationKpiService = dataValidationKpiService;
        this.exceptionFactory = exceptionFactory;
        this.meteringGroupsService = meteringGroupsService;
        this.dataValidationKpiInfoFactory = dataValidationKpiInfoFactory;
        this.conflictFactory = conflictFactory;
    }

    @GET
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_VALIDATION_CONFIGURATION, Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION})
    public PagedInfoList getAllDataValidationKpis(@BeanParam JsonQueryParameters queryParameters) {
        List<DataValidationKpiInfo> dataValidations = dataValidationKpiService.dataValidationKpiFinder()
                .from(queryParameters)
                .stream()
                .map(dataValidationKpiInfoFactory::from)
                .collect(toList());

        return PagedInfoList.fromPagedList("kpis", dataValidations, queryParameters);
    }


    @GET
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/{id}")
    @RolesAllowed({Privileges.Constants.VIEW_VALIDATION_CONFIGURATION, Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION})
    public DataValidationKpiInfo getDataValidationKpiById(@PathParam("id") long id) {
        DataValidationKpi dataValidationKpi = dataValidationKpiService.findDataValidationKpi(id)
                .orElseThrow(() -> new WebApplicationException("No DeviceProtocolPluggableClass with id " + id, Response.Status.NOT_FOUND));
        return dataValidationKpiInfoFactory.from(dataValidationKpi);
    }

    @GET
    @Transactional
    @Path("/groups")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_VALIDATION_CONFIGURATION, Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION})
    public Response getAvailableDeviceGroups(@BeanParam JsonQueryParameters queryParameters) {
        List<EndDeviceGroup> allGroups = meteringGroupsService.getEndDeviceGroupQuery().select(Condition.TRUE, Order.ascending("upper(name)"));
        List<Long> usedGroupIds = dataValidationKpiService.findAllDataValidationKpis().stream().map(kpi -> kpi.getDeviceGroup().getId()).collect(Collectors
                .toList());
        Iterator<EndDeviceGroup> groupIterator = allGroups.iterator();
        while (groupIterator.hasNext()) {
            EndDeviceGroup next = groupIterator.next();
            if (usedGroupIds.contains(next.getId())) {
                groupIterator.remove();
            }
        }
        return Response.ok(PagedInfoList.fromPagedList("deviceGroups", allGroups.stream()
                .map(gr -> new LongIdWithNameInfo(gr.getId(), gr.getName())).collect(Collectors.toList()), queryParameters)).build();
    }

    @GET
    @Transactional
    @Path("/kpigroups")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_VALIDATION_CONFIGURATION, Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION})
    public Response getDeviceGroupsWithValidationKpi(@BeanParam JsonQueryParameters queryParameters) {
        List<EndDeviceGroup> usedGroups = dataValidationKpiService.findAllDataValidationKpis()
                .stream()
                .filter(kpi -> kpi.getLatestCalculation().isPresent())
                .map(kpi -> kpi.getDeviceGroup().getId())
                .map(meteringGroupsService::findEndDeviceGroup)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        return Response.ok(PagedInfoList.fromPagedList("deviceGroups", usedGroups.stream()
                .map(gr -> new LongIdWithNameInfo(gr.getId(), gr.getName())).collect(Collectors.toList()), queryParameters)).build();
    }

    @DELETE
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/{id}")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION})
    public Response deleteDataValidationKpi(@PathParam("id") long id, DataValidationKpiInfo info) {
        info.id = id;
        DataValidationKpi lockedDataValidationKpi = getLockedDataValidationKpi(info.id, info.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(info.deviceGroup.name)
                        .withActualVersion(() -> getCurrentDataValidationKpiVersion(info.id))
                        .supplier());
        lockedDataValidationKpi.delete();
        return Response.ok().build();
    }

    @POST
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_VALIDATION_CONFIGURATION})
    public Response createDataValidationKpi(DataValidationKpiInfo kpiInfo) {

        EndDeviceGroup endDeviceGroup = null;
        if (kpiInfo.deviceGroup != null && kpiInfo.deviceGroup.id != null) {
            endDeviceGroup = meteringGroupsService.findEndDeviceGroup(kpiInfo.deviceGroup.id)
                    .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_SUCH_DEVICE_GROUP, kpiInfo.deviceGroup.id));
        }
        DataValidationKpiService.DataValidationKpiBuilder dataValidationKpiBuilder = dataValidationKpiService.newDataValidationKpi(endDeviceGroup);
        if (kpiInfo.frequency != null && kpiInfo.frequency.every != null && kpiInfo.frequency.every.asTimeDuration() != null) {
            dataValidationKpiBuilder.frequency(kpiInfo.frequency.every.asTimeDuration().asTemporalAmount());
        }

        DataValidationKpi dataValidationKpi = dataValidationKpiBuilder.build();
        return Response.status(Response.Status.CREATED).entity(dataValidationKpiInfoFactory.from(dataValidationKpi)).build();
    }

    private Optional<DataValidationKpi> getLockedDataValidationKpi(long id, long version) {
        return dataValidationKpiService.findAndLockDataValidationKpiByIdAndVersion(id, version);
    }

    private Long getCurrentDataValidationKpiVersion(long id) {
        return dataValidationKpiService.findDataValidationKpi(id).map(DataValidationKpi::getVersion).orElse(null);
    }

}
