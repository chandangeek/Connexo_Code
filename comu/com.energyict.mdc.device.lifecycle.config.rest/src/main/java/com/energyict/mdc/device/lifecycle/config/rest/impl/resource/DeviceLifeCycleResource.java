package com.energyict.mdc.device.lifecycle.config.rest.impl.resource;

import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.lifecycle.config.Privileges;
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
    private final DeviceLifeCyclePrivilegeFactory deviceLifeCyclePrivilegeFactory;
    private final StateTransitionEventTypeFactory stateTransitionEventTypeFactory;

    @Inject
    public DeviceLifeCycleResource(DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService, FiniteStateMachineService finiteStateMachineService, ResourceHelper resourceHelper, Provider<DeviceLifeCycleStateResource> lifeCycleStateResourceProvider, Provider<DeviceLifeCycleActionResource> lifeCycleStateTransitionsResourceProvider, DeviceLifeCyclePrivilegeFactory deviceLifeCyclePrivilegeFactory, StateTransitionEventTypeFactory stateTransitionEventTypeFactory) {
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
        this.finiteStateMachineService = finiteStateMachineService;
        this.resourceHelper = resourceHelper;
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
                .map(DeviceLifeCycleInfo::new).collect(Collectors.toList());
        return PagedInfoList.fromPagedList("deviceLifeCycles", lifecycles, queryParams);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE_LIFE_CYCLE})
    public Response getDeviceLifeCycleById(@PathParam("id") Long id, @BeanParam JsonQueryParameters queryParams) {
        DeviceLifeCycle lifeCycle = resourceHelper.findDeviceLifeCycleByIdOrThrowException(id);
        return Response.ok(new DeviceLifeCycleInfo(lifeCycle)).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.CONFIGURE_DEVICE_LIFE_CYCLE})
    public Response addDeviceLifeCycle(DeviceLifeCycleInfo deviceLifeCycleInfo) {
        DeviceLifeCycle newLifeCycle = deviceLifeCycleConfigurationService.newDefaultDeviceLifeCycle(deviceLifeCycleInfo.name);
        return Response.status(Response.Status.CREATED).entity(new DeviceLifeCycleInfo(newLifeCycle)).build();
    }

    @POST
    @Path("/{id}/clone")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.CONFIGURE_DEVICE_LIFE_CYCLE})
    public Response cloneDeviceLifeCycle(@PathParam("id") Long id, DeviceLifeCycleInfo deviceLifeCycleInfo) {
        DeviceLifeCycle sourceDeviceLifeCycle = resourceHelper.findDeviceLifeCycleByIdOrThrowException(id);
        DeviceLifeCycle clonedLifeCycle = deviceLifeCycleConfigurationService.cloneDeviceLifeCycle(sourceDeviceLifeCycle, deviceLifeCycleInfo.name);
        return Response.status(Response.Status.CREATED).entity(new DeviceLifeCycleInfo(clonedLifeCycle)).build();
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.CONFIGURE_DEVICE_LIFE_CYCLE})
    public Response deleteDeviceLifeCycle(@PathParam("id") Long id) {
        DeviceLifeCycle deviceLifeCycle = resourceHelper.findDeviceLifeCycleByIdOrThrowException(id);
        FiniteStateMachine finiteStateMachine = deviceLifeCycle.getFiniteStateMachine();
        deviceLifeCycle.delete();
        finiteStateMachine.delete();
        return Response.ok(new DeviceLifeCycleInfo(deviceLifeCycle)).build();
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