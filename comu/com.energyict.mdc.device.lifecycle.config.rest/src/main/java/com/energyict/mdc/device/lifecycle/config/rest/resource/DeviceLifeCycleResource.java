package com.energyict.mdc.device.lifecycle.config.rest.resource;

import com.elster.jupiter.fsm.FiniteStateMachine;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.lifecycle.config.Privileges;
import com.energyict.mdc.device.lifecycle.config.rest.response.DeviceLifeCycleInfo;

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
import java.util.List;
import java.util.stream.Collectors;


@Path("/devicelifecycles")
public class DeviceLifeCycleResource {
    private final DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;
    private final ResourceHelper resourceHelper;
    private final Provider<DeviceLifeCycleStateResource> lifeCycleStateResourceProvider;
    private final Provider<DeviceLifeCycleActionResource> lifeCycleStateTransitionsResourceProvider;

    @Inject
    public DeviceLifeCycleResource(DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService, ResourceHelper resourceHelper, Provider<DeviceLifeCycleStateResource> lifeCycleStateResourceProvider, Provider<DeviceLifeCycleActionResource> lifeCycleStateTransitionsResourceProvider) {
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
        this.resourceHelper = resourceHelper;
        this.lifeCycleStateResourceProvider = lifeCycleStateResourceProvider;
        this.lifeCycleStateTransitionsResourceProvider = lifeCycleStateTransitionsResourceProvider;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE_LIFE_CYCLES})
    public PagedInfoList getDeviceLifeCycles(@BeanParam QueryParameters queryParams) {
        List<DeviceLifeCycleInfo> lifecycles = deviceLifeCycleConfigurationService.findAllDeviceLifeCycles().from(queryParams).stream()
                .map(DeviceLifeCycleInfo::new).collect(Collectors.toList());
        return PagedInfoList.fromPagedList("deviceLifeCycles", lifecycles, queryParams);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE_LIFE_CYCLES})
    public Response getDeviceLifeCycleById(@PathParam("id") Long id, @BeanParam QueryParameters queryParams) {
        DeviceLifeCycle lifeCycle = resourceHelper.findDeviceLifeCycleByIdOrThrowException(id);
        return Response.ok(new DeviceLifeCycleInfo(lifeCycle)).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.CONFIGURE_DEVICE_LIFE_CYCLES})
    public Response addDeviceLifeCycle(DeviceLifeCycleInfo deviceLifeCycleInfo) {
        DeviceLifeCycle newLifeCycle = deviceLifeCycleConfigurationService.newDefaultDeviceLifeCycle(deviceLifeCycleInfo.name);
        return Response.status(Response.Status.CREATED).entity(new DeviceLifeCycleInfo(newLifeCycle)).build();
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.CONFIGURE_DEVICE_LIFE_CYCLES})
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
}