package com.energyict.mdc.device.lifecycle.config.rest.resource;

import com.elster.jupiter.fsm.State;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.lifecycle.config.Privileges;
import com.energyict.mdc.device.lifecycle.config.rest.response.LifeCycleStateFactory;
import com.energyict.mdc.device.lifecycle.config.rest.response.LifeCycleStateInfo;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class LifeCycleStateResource {
    private final DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;
    private final LifeCycleStateFactory lifeCycleStateFactory;
    private final ResourceHelper resourceHelper;

    @Inject
    public LifeCycleStateResource(DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService, LifeCycleStateFactory lifeCycleStateFactory, ResourceHelper resourceHelper) {
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
        this.lifeCycleStateFactory = lifeCycleStateFactory;
        this.resourceHelper = resourceHelper;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE_LIFE_CYCLES})
    public PagedInfoList getStatesForDeviceLifecycle(@PathParam("cycleId") Long lifeCycleId, @BeanParam QueryParameters queryParams) {
        List<LifeCycleStateInfo> states = resourceHelper.findDeviceLifeCycleByIdOrThrowException(lifeCycleId).getFiniteStateMachine().getStates()
                .stream()
                .map(state -> lifeCycleStateFactory.from(state))
                .sorted(Comparator.comparing(state -> state.name)) // alphabetical sort
                .collect(Collectors.toList());
        return PagedInfoList.fromPagedList("deviceLifeCycleStates", ListPager.of(states).from(queryParams).find(), queryParams);
    }

    @GET
    @Path("/{stateId}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE_LIFE_CYCLES})
    public Response getStateById(@PathParam("cycleId") Long lifeCycleId, @PathParam("stateId") Long stateId, @BeanParam QueryParameters queryParams) {
        State state = resourceHelper.findStateByIdOrThrowException(resourceHelper.findDeviceLifeCycleByIdOrThrowException(lifeCycleId), stateId);
        return Response.ok(lifeCycleStateFactory.from(state)).build();
    }
}
