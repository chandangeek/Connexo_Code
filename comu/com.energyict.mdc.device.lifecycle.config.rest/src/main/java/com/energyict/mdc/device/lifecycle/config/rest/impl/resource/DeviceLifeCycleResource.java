/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.config.rest.impl.resource;

import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.metering.EventType;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleUpdater;
import com.energyict.mdc.device.lifecycle.config.Privileges;
import com.energyict.mdc.device.lifecycle.config.rest.info.DeviceLifeCycleFactory;
import com.energyict.mdc.device.lifecycle.config.rest.info.DeviceLifeCycleInfo;
import com.energyict.mdc.device.lifecycle.config.rest.info.DeviceLifeCyclePrivilegeFactory;
import com.energyict.mdc.device.lifecycle.config.rest.info.DeviceLifeCyclePrivilegeInfo;
import com.energyict.mdc.device.lifecycle.config.rest.info.DeviceLifeCycleStateSummaryInfo;
import com.energyict.mdc.device.lifecycle.config.rest.info.StateTransitionEventTypeFactory;
import com.energyict.mdc.device.lifecycle.config.rest.info.StateTransitionEventTypeInfo;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Provider;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;


@Path("/devicelifecycles")
public class DeviceLifeCycleResource {

    private static final List<String> eventTypesToExclude = Arrays.asList(
            EventType.METER_CREATED.topic(),
            EventType.METER_UPDATED.topic(),
            EventType.METER_DELETED.topic()
    );
    private static final String PRIVILEGE_VIABLE_DEVICE_LIFECYCLE = "viable.device.lifecycle";

    private final DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;
    private final FiniteStateMachineService finiteStateMachineService;
    private final ResourceHelper resourceHelper;
    private final Provider<DeviceLifeCycleStateResource> lifeCycleStateResourceProvider;
    private final Provider<DeviceLifeCycleActionResource> lifeCycleStateTransitionsResourceProvider;
    private final Provider<TransitionBusinessProcessResource> transitionBusinessProcessResourceProvider;
    private final DeviceLifeCycleFactory deviceLifeCycleFactory;
    private final DeviceLifeCyclePrivilegeFactory deviceLifeCyclePrivilegeFactory;
    private final StateTransitionEventTypeFactory stateTransitionEventTypeFactory;

    @Inject
    public DeviceLifeCycleResource(DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService,
                                   FiniteStateMachineService finiteStateMachineService,
                                   ResourceHelper resourceHelper,
                                   Provider<DeviceLifeCycleStateResource> lifeCycleStateResourceProvider,
                                   Provider<DeviceLifeCycleActionResource> lifeCycleStateTransitionsResourceProvider,
                                   Provider<TransitionBusinessProcessResource> transitionBusinessProcessResourceProvider,
                                   DeviceLifeCycleFactory deviceLifeCycleFactory,
                                   DeviceLifeCyclePrivilegeFactory deviceLifeCyclePrivilegeFactory,
                                   StateTransitionEventTypeFactory stateTransitionEventTypeFactory) {
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
        this.finiteStateMachineService = finiteStateMachineService;
        this.resourceHelper = resourceHelper;
        this.deviceLifeCycleFactory = deviceLifeCycleFactory;
        this.stateTransitionEventTypeFactory = stateTransitionEventTypeFactory;
        this.lifeCycleStateResourceProvider = lifeCycleStateResourceProvider;
        this.transitionBusinessProcessResourceProvider = transitionBusinessProcessResourceProvider;
        this.lifeCycleStateTransitionsResourceProvider = lifeCycleStateTransitionsResourceProvider;
        this.deviceLifeCyclePrivilegeFactory = deviceLifeCyclePrivilegeFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE_LIFE_CYCLE})
    public PagedInfoList getDeviceLifeCycles(@BeanParam JsonQueryParameters queryParams) {
        List<DeviceLifeCycleInfo> lifecycles = deviceLifeCycleConfigurationService.findAllDeviceLifeCycles().from(queryParams).stream()
                .map(deviceLifeCycleFactory::from).collect(Collectors.toList());
        return PagedInfoList.fromPagedList("deviceLifeCycles", lifecycles, queryParams);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE_LIFE_CYCLE})
    public Response getDeviceLifeCycleById(@PathParam("id") Long id, @BeanParam JsonQueryParameters queryParams) {
        DeviceLifeCycle deviceLifeCycle = resourceHelper.findDeviceLifeCycleByIdOrThrowException(id);
        return Response.ok(deviceLifeCycleFactory.from(deviceLifeCycle)).build();
    }

    @GET
    @Path("/{id}/privileges")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE_LIFE_CYCLE})
    public Response getDeviceLifeCycleDynamicPrivileges(@PathParam("id") Long id, @BeanParam JsonQueryParameters queryParams) {
        DeviceLifeCycle deviceLifeCycle = resourceHelper.findDeviceLifeCycleByIdOrThrowException(id);
        List<IdWithNameInfo> privileges = deviceLifeCycle.isObsolete()
                ? Collections.emptyList()
                : Collections.singletonList(PRIVILEGE_VIABLE_DEVICE_LIFECYCLE)
                .stream()
                .map(privilege -> new IdWithNameInfo(null, privilege))
                .collect(Collectors.toList());
        return Response.ok(PagedInfoList.fromCompleteList("privileges", privileges, queryParams)).build();
    }

    @POST
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.CONFIGURE_DEVICE_LIFE_CYCLE})
    public Response addDeviceLifeCycle(DeviceLifeCycleInfo deviceLifeCycleInfo) {
        DeviceLifeCycle newLifeCycle = deviceLifeCycleConfigurationService.newDefaultDeviceLifeCycle(deviceLifeCycleInfo.name);
        return Response.status(Response.Status.CREATED).entity(deviceLifeCycleFactory.from(newLifeCycle)).build();
    }

    @PUT
    @Transactional
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE_LIFE_CYCLE})
    public Response editDeviceLifeCycleById(@PathParam("id") Long id, DeviceLifeCycleInfo info) {
        info.id = id;
        DeviceLifeCycle deviceLifeCycle = resourceHelper.lockDeviceLifeCycleOrThrowException(info);
        DeviceLifeCycleUpdater deviceLifeCycleUpdater = deviceLifeCycle.startUpdate();
        deviceLifeCycleUpdater.setName(info.name).complete().save();
        return Response.ok(deviceLifeCycleFactory.from(deviceLifeCycle)).build();
    }

    @POST
    @Transactional
    @Path("/{id}/clone")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.CONFIGURE_DEVICE_LIFE_CYCLE})
    public Response cloneDeviceLifeCycle(@PathParam("id") Long id, DeviceLifeCycleInfo deviceLifeCycleInfo) {
        DeviceLifeCycle sourceDeviceLifeCycle = resourceHelper.findDeviceLifeCycleByIdOrThrowException(id);
        DeviceLifeCycle clonedLifeCycle = deviceLifeCycleConfigurationService.cloneDeviceLifeCycle(sourceDeviceLifeCycle, deviceLifeCycleInfo.name);
        return Response.status(Response.Status.CREATED).entity(deviceLifeCycleFactory.from(clonedLifeCycle)).build();
    }

    @DELETE
    @Transactional
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.CONFIGURE_DEVICE_LIFE_CYCLE})
    public Response deleteDeviceLifeCycle(@PathParam("id") Long id, DeviceLifeCycleInfo info) {
        info.id = id;
        DeviceLifeCycle deviceLifeCycle = resourceHelper.lockDeviceLifeCycleOrThrowException(info);
        resourceHelper.checkDeviceLifeCycleUsages(deviceLifeCycle);
        FiniteStateMachine fsm = deviceLifeCycle.getFiniteStateMachine();
        deviceLifeCycle.makeObsolete();
        fsm.makeObsolete();
        return Response.ok(deviceLifeCycleFactory.from(deviceLifeCycle)).build();
    }

    @GET
    @Path("/states")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE_LIFE_CYCLE})
    public PagedInfoList getDeviceLifeCycleStateSummary(@BeanParam JsonQueryParameters queryParams) {
        List<DeviceLifeCycleStateSummaryInfo> deviceLifeCycleStateSummary = deviceLifeCycleConfigurationService.findAllDeviceLifeCycles().stream()
                .map(deviceLifeCycleFactory::from)
                .map(lifecycle -> this.lifeCycleStateResourceProvider.get().getAllStatesForDeviceLifecycle(lifecycle.id).stream()
                        .map(state -> new DeviceLifeCycleStateSummaryInfo(lifecycle.id, state.id, lifecycle.name, state.name)))
                .flatMap(y -> y)
                .collect(Collectors.toList());
        return PagedInfoList.fromPagedList("deviceStates", ListPager.of(deviceLifeCycleStateSummary).from(queryParams).find(), queryParams);

    }

    @Path("/{deviceLifeCycleId}/states")
    public DeviceLifeCycleStateResource getLifeCycleStateResource() {
        return this.lifeCycleStateResourceProvider.get();
    }

    @Path("/{deviceLifeCycleId}/actions")
    public DeviceLifeCycleActionResource getLifeCycleTransitionsResource() {
        return this.lifeCycleStateTransitionsResourceProvider.get();
    }

    @Path("/statechangebusinessprocesses")
    public TransitionBusinessProcessResource getTransitionBusinessProcessResource() {
        return this.transitionBusinessProcessResourceProvider.get();
    }

    @GET
    @Path("/privileges")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE_LIFE_CYCLE})
    public PagedInfoList getPrivilegesList(@BeanParam JsonQueryParameters queryParams) {
        List<DeviceLifeCyclePrivilegeInfo> privileges = EnumSet.allOf(AuthorizedAction.Level.class)
                .stream()
                .map(deviceLifeCyclePrivilegeFactory::from)
                .collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("privileges", privileges, queryParams);
    }

    @GET
    @Path("/eventtypes")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE_LIFE_CYCLE})
    public PagedInfoList getEventTypesList(@BeanParam JsonQueryParameters queryParams) {
        List<StateTransitionEventTypeInfo> eventTypes = finiteStateMachineService.getStateTransitionEventTypes(DeviceLifeCycleConfigurationService.COMPONENT_NAME, "System")
                .stream()
                .filter(eventType -> !eventTypesToExclude.contains(eventType.getSymbol()))//we exclude meter specific event types because in MDC we have other ones which duplicates base
                .map(stateTransitionEventTypeFactory::from)
                .collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("eventTypes", eventTypes, queryParams);
    }
}