/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.kpi.rest;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.LongIdWithNameInfo;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.kpi.rest.impl.RegisteredDeviceKpiGroupInfo;
import com.energyict.mdc.device.data.kpi.rest.impl.RegisteredDevicesKpiGraphInfo;
import com.energyict.mdc.device.data.kpi.rest.impl.RegisteredDevicesKpiInfo;
import com.energyict.mdc.device.data.kpi.rest.impl.TaskInfo;
import com.energyict.mdc.device.data.rest.impl.MessageSeeds;
import com.energyict.mdc.device.data.rest.impl.ResourceHelper;
import com.energyict.mdc.device.topology.kpi.Privileges;
import com.energyict.mdc.device.topology.kpi.RegisteredDevicesKpi;
import com.energyict.mdc.device.topology.kpi.RegisteredDevicesKpiFrequency;
import com.energyict.mdc.device.topology.kpi.RegisteredDevicesKpiScore;
import com.energyict.mdc.device.topology.kpi.RegisteredDevicesKpiService;

import com.google.common.collect.Range;

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
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Path("/registereddevkpis")
public class RegisteredDevicesKpiResource {
    private final RegisteredDevicesKpiService registeredDevicesKpiService;
    private final MeteringGroupsService meteringGroupsService;
    private final ExceptionFactory exceptionFactory;
    private final RegisteredDevicesKpiInfoFactory registeredDevicesKpiInfoFactory;
    private final ResourceHelper resourceHelper;
    private final DeviceService deviceService;
    private final com.elster.jupiter.tasks.TaskService tskService;
    private final Clock clock;

    @Inject
    public RegisteredDevicesKpiResource(RegisteredDevicesKpiService registeredDevicesKpiService, MeteringGroupsService meteringGroupsService, ExceptionFactory exceptionFactory, RegisteredDevicesKpiInfoFactory registeredDevicesKpiInfoFactory, ResourceHelper resourceHelper, DeviceService deviceService, Clock clock, com.elster.jupiter.tasks.TaskService tskService) {
        this.registeredDevicesKpiService = registeredDevicesKpiService;
        this.meteringGroupsService = meteringGroupsService;
        this.exceptionFactory = exceptionFactory;
        this.registeredDevicesKpiInfoFactory = registeredDevicesKpiInfoFactory;
        this.resourceHelper = resourceHelper;
        this.deviceService = deviceService;
        this.clock = clock;
        this.tskService = tskService;
    }

    @POST
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE})
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
        registeredDevicesKpiBuilder.setNextRecurrentTasks(this.findRecurrentTaskOrThrowException(kpiInfo.nextRecurrentTasks));


        RegisteredDevicesKpi registeredDevicesKpi = registeredDevicesKpiBuilder.save();
        return Response.status(Response.Status.CREATED).entity(registeredDevicesKpiInfoFactory.from(registeredDevicesKpi)).build();
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE, Privileges.Constants.VIEW,
            com.energyict.mdc.device.data.security.Privileges.Constants.VIEW_DEVICE,
            com.energyict.mdc.device.data.security.Privileges.Constants.OPERATE_DEVICE_COMMUNICATION,
            com.energyict.mdc.device.data.security.Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Response getKpis(@BeanParam JsonQueryParameters queryParameters) {
        if (queryParameters.getStart().isPresent() && queryParameters.getLimit().isPresent()) {
            List<RegisteredDevicesKpiInfo> collection = registeredDevicesKpiService.registeredDevicesKpiFinder()
                    .from(queryParameters)
                    .stream()
                    .map(registeredDevicesKpiInfoFactory::from)
                    .collect(toList());
            return Response.ok(PagedInfoList.fromPagedList("kpis", collection, queryParameters)).build();
        } else {
            List<RegisteredDeviceKpiGroupInfo> infos = registeredDevicesKpiService.findAllRegisteredDevicesKpis()
                    .stream()
                    .map(RegisteredDeviceKpiGroupInfo::new)
                    .sorted((info1, info2) -> info1.name.compareToIgnoreCase(info2.name))
                    .collect(toList());
            return Response.ok(infos).build();
        }
    }

    @GET
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/{id}")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE, Privileges.Constants.VIEW})
    public RegisteredDevicesKpiInfo getKpiById(@PathParam("id") long id) {
        RegisteredDevicesKpi registeredDevicesKpi = resourceHelper.findRegisteredDevicesKpiByIdOrThrowException(id);
        return registeredDevicesKpiInfoFactory.from(registeredDevicesKpi);
    }

    @GET
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/recurrenttask/{id}")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE, Privileges.Constants.VIEW})
    public RegisteredDevicesKpiInfo getKpiByRecurrentTaskId(@PathParam("id") long id) {
        RegisteredDevicesKpi registeredDevicesKpi = resourceHelper.findRegisteredDevicesKpiByRecurrentTaskIdOrThrowException(id);
        return registeredDevicesKpiInfoFactory.from(registeredDevicesKpi);
    }

    @GET
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/kpidata")
    @RolesAllowed({com.energyict.mdc.device.data.security.Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION,
            com.energyict.mdc.device.data.security.Privileges.Constants.OPERATE_DEVICE_COMMUNICATION,
            com.energyict.mdc.device.data.security.Privileges.Constants.VIEW_DEVICE,})
    public Response getKpiData(@BeanParam JsonQueryFilter filter) {
        RegisteredDevicesKpi registeredDevicesKpi = filter.hasProperty("kpiId")
            ? resourceHelper.findRegisteredDevicesKpiByIdOrThrowException(filter.getLong("kpiId"))
            : null;
        Optional<Range<Instant>> range = Optional.empty();
        if (filter.hasProperty("start") && filter.hasProperty("end")) {
            Instant start = filter.getInstant("start");
            Instant end = filter.getInstant("end");
            range = Optional.of(Range.closed(start, end));
        } else if (registeredDevicesKpi!=null) {
            range = calculateRange(registeredDevicesKpi.getLatestCalculation(), registeredDevicesKpi.getFrequency());
        }
        List<RegisteredDevicesKpiScore> scores = new ArrayList<>();
        if (range.isPresent()) {
            scores = registeredDevicesKpi.getScores(range.get());
        }
        List<RegisteredDevicesKpiGraphInfo> infos = scores.stream()
                .sorted()
                .map(score -> new RegisteredDevicesKpiGraphInfo(score, registeredDevicesKpi.getTarget()))
                .collect(Collectors.toList());
        return Response.ok(infos).build();
    }

    private Optional<Range<Instant>> calculateRange(Optional<Instant> latestCalculation, TemporalAmount frequency) {
        if (latestCalculation.isPresent()) {
            Optional<RegisteredDevicesKpiFrequency> registeredDevicesKpiFrequency = RegisteredDevicesKpiFrequency.valueOf(frequency);
            if (registeredDevicesKpiFrequency.isPresent()) {
                RegisteredDevicesKpiFrequency freq = registeredDevicesKpiFrequency.get();
                Range<Instant> range;
                switch (freq) {
                    case FIFTEEN_MINUTES:
                    case FOUR_HOURS:
                        range = Range.closed(latestCalculation.get().minus(1, ChronoUnit.DAYS), latestCalculation.get());
                        break;
                    case TWELVE_HOURS:
                        range = Range.closed(latestCalculation.get().minus(7, ChronoUnit.DAYS), latestCalculation.get());
                        break;
                    case ONE_DAY:
                        range = Range.closed(latestCalculation.get().minus(30, ChronoUnit.DAYS), latestCalculation.get());
                        break;
                    default:
                        range = Range.closed(latestCalculation.get().minus(1, ChronoUnit.DAYS), latestCalculation.get());
                        break;
                }
                return Optional.of(range);
            }
        }
        return Optional.empty();
    }

    @DELETE
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/{id}")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE})
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
    @RolesAllowed({Privileges.Constants.ADMINISTRATE})
    public Response updateKpi(@PathParam("id") long id, RegisteredDevicesKpiInfo info) {
        info.id = id;
        RegisteredDevicesKpi kpi = resourceHelper.lockRegisteredDevicesKpiOrThrowException(info);

        kpi.setTarget(info.target);
        kpi.setNextRecurrentTasks(this.findRecurrentTaskOrThrowException(info.nextRecurrentTasks));
        kpi.save();

        return Response.ok(registeredDevicesKpiInfoFactory.from(registeredDevicesKpiService.findRegisteredDevicesKpi(id).get())).build();
    }

    @GET
    @Transactional
    @Path("/groups")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE})
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

    @GET
    @Transactional
    @Path("/gateway/{name}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response getKpiForGateway(@PathParam("name") String name, @BeanParam JsonQueryFilter filter) {
        Device device = deviceService.findDeviceByName(name).orElseThrow(() -> exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.NO_SUCH_DEVICE));
        Optional<Range<Instant>> range;
        List<RegisteredDevicesKpiScore> scores = new ArrayList<>();
        RegisteredDevicesKpiFrequency frequency = RegisteredDevicesKpiFrequency.FIFTEEN_MINUTES;
        if (filter.hasProperty("frequency")) {
            switch(filter.getString("frequency")) {
                case "15minutes":
                    frequency = RegisteredDevicesKpiFrequency.FIFTEEN_MINUTES;
                    break;
                case "4hours":
                    frequency = RegisteredDevicesKpiFrequency.FOUR_HOURS;
                    break;
                case "12hours":
                    frequency = RegisteredDevicesKpiFrequency.TWELVE_HOURS;
                    break;
                case "1days":
                default:
                    frequency = RegisteredDevicesKpiFrequency.ONE_DAY;
                    break;
            }
        }
        if (filter.hasProperty("start") && filter.hasProperty("end")) {
            Instant start = filter.getInstant("start");
            Instant end = filter.getInstant("end");
            range = Optional.of(Range.closed(start, end));
        } else {
            range = calculateRange(Optional.of(Instant.now(clock)), frequency.getFrequency());
        }
        if (range.isPresent()) {
            scores = registeredDevicesKpiService.getScores(device, range.get(), frequency);
        }
        List<RegisteredDevicesKpiGraphInfo> infos = scores.stream()
                .sorted()
                .map(RegisteredDevicesKpiGraphInfo::new)
                .collect(Collectors.toList());
        return Response.ok(infos).build();
    }

    private List<RecurrentTask> findRecurrentTaskOrThrowException(List<TaskInfo> nextRecurrentTasks) {
        List<RecurrentTask> recurrentTasks = new ArrayList<>();
        if (nextRecurrentTasks != null) {
            nextRecurrentTasks.forEach(taskInfo -> {
                recurrentTasks.add(tskService.getRecurrentTask(taskInfo.id)
                        .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND)));

            });
        }
        return recurrentTasks;
    }
}
