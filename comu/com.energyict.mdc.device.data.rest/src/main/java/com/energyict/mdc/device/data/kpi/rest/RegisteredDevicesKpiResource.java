/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.kpi.rest;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.energyict.mdc.device.data.rest.impl.MessageSeeds;
import com.energyict.mdc.device.data.rest.impl.ResourceHelper;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.device.topology.kpi.RegisteredDevicesKpi;
import com.energyict.mdc.device.topology.kpi.RegisteredDevicesKpiService;

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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Path("/registereddevkpis")
public class RegisteredDevicesKpiResource {
    private final RegisteredDevicesKpiService registeredDevicesKpiService;
    private final MeteringGroupsService meteringGroupsService;
    private final ExceptionFactory exceptionFactory;
    private final RegisteredDevicesKpiInfoFactory registeredDevicesKpiInfoFactory;
    private final ResourceHelper resourceHelper;

    @Inject
    public RegisteredDevicesKpiResource(RegisteredDevicesKpiService registeredDevicesKpiService, MeteringGroupsService meteringGroupsService, ExceptionFactory exceptionFactory, RegisteredDevicesKpiInfoFactory registeredDevicesKpiInfoFactory, ResourceHelper resourceHelper) {
        this.registeredDevicesKpiService = registeredDevicesKpiService;
        this.meteringGroupsService = meteringGroupsService;
        this.exceptionFactory = exceptionFactory;
        this.registeredDevicesKpiInfoFactory = registeredDevicesKpiInfoFactory;
        this.resourceHelper = resourceHelper;
    }

    @POST
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response createKpi(RegisteredDevicesKpiInfo kpiInfo) {
        EndDeviceGroup endDeviceGroup = null;
        if (kpiInfo.deviceGroup != null && kpiInfo.deviceGroup.id != null) {
            endDeviceGroup = meteringGroupsService.findEndDeviceGroup(kpiInfo.deviceGroup.id)
                    .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_SUCH_DEVICE_GROUP, kpiInfo.deviceGroup.id));
        }
        RegisteredDevicesKpiService.RegisteredDevicesKpiBuilder registeredDevicesKpiBuilder = registeredDevicesKpiService.newRegisteredDevicesKpi(endDeviceGroup);
        if (kpiInfo.frequency != null && kpiInfo.frequency.every != null && kpiInfo.frequency.every.asTimeDuration() != null) {
            registeredDevicesKpiBuilder.frequency(kpiInfo.frequency.every.asTimeDuration().asTemporalAmount());
        }
        registeredDevicesKpiBuilder.target(kpiInfo.target);


        RegisteredDevicesKpi registeredDevicesKpi = registeredDevicesKpiBuilder.save();
        return Response.status(Response.Status.CREATED).entity(registeredDevicesKpiInfoFactory.from(registeredDevicesKpi)).build();
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getKpis(@BeanParam JsonQueryParameters queryParameters) {
        List<RegisteredDevicesKpiInfo> collection = registeredDevicesKpiService.registeredDevicesKpiFinder()
                .from(queryParameters)
                .stream()
                .map(registeredDevicesKpiInfoFactory::from)
                .collect(toList());

        return PagedInfoList.fromPagedList("kpis", collection, queryParameters);
    }

    @GET
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/{id}")
    public RegisteredDevicesKpiInfo getKpiById(@PathParam("id") long id) {
        RegisteredDevicesKpi registeredDevicesKpi = resourceHelper.findRegisteredDevicesKpiByIdOrThrowException(id);
        return registeredDevicesKpiInfoFactory.from(registeredDevicesKpi);
    }

    @DELETE
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/{id}")
    @RolesAllowed({Privileges.Constants.ADMINISTER_DATA_COLLECTION_KPI})
    public Response deleteKpi(@PathParam("id") long id, RegisteredDevicesKpiInfo info) {
        info.id = id;
        resourceHelper.lockRegisteredDevicesKpiOrThrowException(info).delete();
        return Response.ok().build();
    }

    @PUT
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/{id}")
    public Response updateKpi(@PathParam("id") long id, RegisteredDevicesKpiInfo info) {
        info.id = id;
        RegisteredDevicesKpi kpi = resourceHelper.lockRegisteredDevicesKpiOrThrowException(info);

        kpi.updateTarget(info.target);

        return Response.ok(registeredDevicesKpiInfoFactory.from(registeredDevicesKpiService.findRegisteredDevicesKpi(id).get())).build();
    }

    @GET
    @Transactional
    @Path("/groups")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response getAvailableDeviceGroups(@BeanParam JsonQueryParameters queryParameters) {
        List<EndDeviceGroup> allGroups = meteringGroupsService.getEndDeviceGroupQuery().select(Condition.TRUE, Order.ascending("upper(name)"));
        List<Long> usedGroupIds =
                registeredDevicesKpiService
                        .findAllRegisteredDevicesKpis()
                        .stream()
                        .map(RegisteredDevicesKpi::getDeviceGroup)
                        .map(HasId::getId)
                        .collect(Collectors.toList());
        allGroups.removeIf(group -> usedGroupIds.contains(group.getId()));
        return Response.ok(PagedInfoList.fromPagedList("deviceGroups", allGroups.stream()
                .map(gr -> new LongIdWithNameInfo(gr.getId(), gr.getName())).collect(Collectors.toList()), queryParameters)).build();
    }
}
