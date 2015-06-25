package com.energyict.mdc.device.lifecycle.config.rest.impl.resource;

import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleUpdater;
import com.energyict.mdc.device.lifecycle.config.Privileges;
import com.energyict.mdc.device.lifecycle.config.rest.info.DeviceLifeCycleFactory;
import com.energyict.mdc.device.lifecycle.config.rest.info.DeviceLifeCycleInfo;
import com.energyict.mdc.device.lifecycle.config.rest.info.DeviceLifeCyclePrivilegeFactory;
import com.energyict.mdc.device.lifecycle.config.rest.info.DeviceLifeCyclePrivilegeInfo;
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
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;


@Path("/devicelifecycles")
public class DeviceLifeCycleResource {
    private final DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;
    private final FiniteStateMachineService finiteStateMachineService;
    private final ResourceHelper resourceHelper;
    private final Provider<DeviceLifeCycleStateResource> lifeCycleStateResourceProvider;
    private final Provider<DeviceLifeCycleActionResource> lifeCycleStateTransitionsResourceProvider;
    private final DeviceLifeCycleFactory deviceLifeCycleFactory;
    private final DeviceLifeCyclePrivilegeFactory deviceLifeCyclePrivilegeFactory;
    private final StateTransitionEventTypeFactory stateTransitionEventTypeFactory;

    @Inject
    public DeviceLifeCycleResource(DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService, FiniteStateMachineService finiteStateMachineService, ResourceHelper resourceHelper, Provider<DeviceLifeCycleStateResource> lifeCycleStateResourceProvider, Provider<DeviceLifeCycleActionResource> lifeCycleStateTransitionsResourceProvider, DeviceLifeCycleFactory deviceLifeCycleFactory, DeviceLifeCyclePrivilegeFactory deviceLifeCyclePrivilegeFactory, StateTransitionEventTypeFactory stateTransitionEventTypeFactory) {
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
        this.finiteStateMachineService = finiteStateMachineService;
        this.resourceHelper = resourceHelper;
        this.deviceLifeCycleFactory = deviceLifeCycleFactory;
        this.stateTransitionEventTypeFactory = stateTransitionEventTypeFactory;
        this.lifeCycleStateResourceProvider = lifeCycleStateResourceProvider;
        this.lifeCycleStateTransitionsResourceProvider = lifeCycleStateTransitionsResourceProvider;
        this.deviceLifeCyclePrivilegeFactory = deviceLifeCyclePrivilegeFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE_LIFE_CYCLE})
    public PagedInfoList getDeviceLifeCycles(@BeanParam JsonQueryParameters queryParams) {
        List<DeviceLifeCycleInfo> lifecycles = deviceLifeCycleConfigurationService.findAllDeviceLifeCycles().from(queryParams).stream()
                .map(deviceLifeCycleFactory::from).collect(Collectors.toList());
        return PagedInfoList.fromPagedList("deviceLifeCycles", lifecycles, queryParams);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE_LIFE_CYCLE})
    public Response getDeviceLifeCycleById(@PathParam("id") Long id, @BeanParam JsonQueryParameters queryParams) {
        DeviceLifeCycle deviceLifeCycle = resourceHelper.findDeviceLifeCycleByIdOrThrowException(id);
        return Response.ok(deviceLifeCycleFactory.from(deviceLifeCycle)).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.CONFIGURE_DEVICE_LIFE_CYCLE})
    public Response addDeviceLifeCycle(DeviceLifeCycleInfo deviceLifeCycleInfo) {
        DeviceLifeCycle newLifeCycle = deviceLifeCycleConfigurationService.newDefaultDeviceLifeCycle(deviceLifeCycleInfo.name);
        return Response.status(Response.Status.CREATED).entity(deviceLifeCycleFactory.from(newLifeCycle)).build();
    }

    @PUT
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE_LIFE_CYCLE})
    public Response editDeviceLifeCycleById(@PathParam("id") Long id, DeviceLifeCycleInfo deviceLifeCycleInfo) {
        DeviceLifeCycle deviceLifeCycle = resourceHelper.findDeviceLifeCycleByIdOrThrowException(id);
        DeviceLifeCycleUpdater deviceLifeCycleUpdater = deviceLifeCycle.startUpdate();
        deviceLifeCycleUpdater.setName(deviceLifeCycleInfo.name).complete().save();
        return Response.ok(deviceLifeCycleFactory.from(deviceLifeCycle)).build();
    }

    @POST
    @Path("/{id}/clone")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.CONFIGURE_DEVICE_LIFE_CYCLE})
    public Response cloneDeviceLifeCycle(@PathParam("id") Long id, DeviceLifeCycleInfo deviceLifeCycleInfo) {
        DeviceLifeCycle sourceDeviceLifeCycle = resourceHelper.findDeviceLifeCycleByIdOrThrowException(id);
        DeviceLifeCycle clonedLifeCycle = deviceLifeCycleConfigurationService.cloneDeviceLifeCycle(sourceDeviceLifeCycle, deviceLifeCycleInfo.name);
        return Response.status(Response.Status.CREATED).entity(deviceLifeCycleFactory.from(clonedLifeCycle)).build();
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.CONFIGURE_DEVICE_LIFE_CYCLE})
    public Response deleteDeviceLifeCycle(@PathParam("id") Long id) {
        DeviceLifeCycle deviceLifeCycle = resourceHelper.findDeviceLifeCycleByIdOrThrowException(id);
        resourceHelper.checkDeviceLifeCycleUsages(deviceLifeCycle);
        FiniteStateMachine finiteStateMachine = deviceLifeCycle.getFiniteStateMachine();
        deviceLifeCycle.makeObsolete();
        finiteStateMachine.makeObsolete();
        return Response.ok(deviceLifeCycleFactory.from(deviceLifeCycle)).build();
    }

    @Path("/{deviceLifeCycleId}/states")
    public DeviceLifeCycleStateResource getLifeCycleStateResource() {
        return this.lifeCycleStateResourceProvider.get();
    }

    @Path("/{deviceLifeCycleId}/actions")
    public DeviceLifeCycleActionResource getLifeCycleTransitionsResource() {
        return this.lifeCycleStateTransitionsResourceProvider.get();
    }

    @GET
    @Path("/privileges")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE_LIFE_CYCLE})
    public PagedInfoList getPrivilegesList(@BeanParam JsonQueryParameters queryParams) {
        List<DeviceLifeCyclePrivilegeInfo> privileges = EnumSet.allOf(AuthorizedAction.Level.class)
                .stream()
                .map(deviceLifeCyclePrivilegeFactory::from)
                .collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("privileges", privileges, queryParams);
    }

    @GET
    @Path("/eventtypes")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE_LIFE_CYCLE})
    public PagedInfoList getEventTypesList(@BeanParam JsonQueryParameters queryParams) {
        List<StateTransitionEventTypeInfo> eventTypes = finiteStateMachineService.getStateTransitionEventTypes().stream()
                .map(stateTransitionEventTypeFactory::from)
                .collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("eventTypes", eventTypes, queryParams);
    }
}