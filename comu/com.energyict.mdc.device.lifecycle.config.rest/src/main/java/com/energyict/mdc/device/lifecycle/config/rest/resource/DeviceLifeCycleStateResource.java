package com.energyict.mdc.device.lifecycle.config.rest.resource;

import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.FiniteStateMachineUpdater;
import com.elster.jupiter.fsm.State;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.lifecycle.config.Privileges;
import com.energyict.mdc.device.lifecycle.config.rest.response.DeviceLifeCycleStateFactory;
import com.energyict.mdc.device.lifecycle.config.rest.response.DeviceLifeCycleStateInfo;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DeviceLifeCycleStateResource {
    private final ExceptionFactory exceptionFactory;
    private final DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;
    private final DeviceLifeCycleStateFactory deviceLifeCycleStateFactory;
    private final ResourceHelper resourceHelper;

    @Inject
    public DeviceLifeCycleStateResource(
            ExceptionFactory exceptionFactory,
            DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService,
            DeviceLifeCycleStateFactory deviceLifeCycleStateFactory,
            ResourceHelper resourceHelper) {
        this.exceptionFactory = exceptionFactory;
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
        this.deviceLifeCycleStateFactory = deviceLifeCycleStateFactory;
        this.resourceHelper = resourceHelper;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE_LIFE_CYCLES})
    public PagedInfoList getStatesForDeviceLifecycle(@PathParam("deviceLifeCycleId") Long deviceLifeCycleId, @BeanParam QueryParameters queryParams) {
        List<DeviceLifeCycleStateInfo> states = resourceHelper.findDeviceLifeCycleByIdOrThrowException(deviceLifeCycleId).getFiniteStateMachine().getStates()
                .stream()
                .map(deviceLifeCycleStateFactory::from)
                .sorted(Comparator.comparing(state -> state.name)) // alphabetical sort
                .collect(Collectors.toList());
        return PagedInfoList.fromPagedList("deviceLifeCycleStates", ListPager.of(states).from(queryParams).find(), queryParams);
    }

    @GET
    @Path("/{stateId}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE_LIFE_CYCLES})
    public Response getStateById(@PathParam("deviceLifeCycleId") Long deviceLifeCycleId, @PathParam("stateId") Long stateId, @BeanParam QueryParameters queryParams) {
        State state = resourceHelper.findStateByIdOrThrowException(resourceHelper.findDeviceLifeCycleByIdOrThrowException(deviceLifeCycleId), stateId);
        return Response.ok(deviceLifeCycleStateFactory.from(state)).build();
    }


    @POST
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.CONFIGURE_DEVICE_LIFE_CYCLES})
    public Response addDeviceLifeCycleState(@PathParam("deviceLifeCycleId") Long deviceLifeCycleId, DeviceLifeCycleStateInfo stateInfo) {
        DeviceLifeCycle deviceLifeCycle = resourceHelper.findDeviceLifeCycleByIdOrThrowException(deviceLifeCycleId);
        FiniteStateMachineUpdater fsmUpdater = deviceLifeCycle.getFiniteStateMachine().startUpdate();
        fsmUpdater.newCustomState(stateInfo.name).complete();
        FiniteStateMachine fsm = fsmUpdater.complete();
        Optional<State> newState = fsm.getState(stateInfo.name);
        return Response.status(Response.Status.CREATED).entity(deviceLifeCycleStateFactory.from(newState.get())).build();
    }

    @PUT
    @Path("/{stateId}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.CONFIGURE_DEVICE_LIFE_CYCLES})
    public Response editDeviceLifeCycleState(@PathParam("deviceLifeCycleId") Long deviceLifeCycleId, @PathParam("stateId") Long stateId, DeviceLifeCycleStateInfo stateInfo) {
        DeviceLifeCycle deviceLifeCycle = resourceHelper.findDeviceLifeCycleByIdOrThrowException(deviceLifeCycleId);
        State stateForEdit = resourceHelper.findStateByIdOrThrowException(deviceLifeCycle, stateId);

        FiniteStateMachineUpdater fsmUpdater = deviceLifeCycle.getFiniteStateMachine().startUpdate();
        FiniteStateMachineUpdater.StateUpdater stateUpdater = fsmUpdater.state(stateId);
        if (stateForEdit.isCustom()) {
            stateUpdater.setName(stateInfo.name);
        }
        State stateAfterEdit = stateUpdater.complete();
        fsmUpdater.complete();
        return Response.ok(deviceLifeCycleStateFactory.from(stateAfterEdit)).build();
    }
}
