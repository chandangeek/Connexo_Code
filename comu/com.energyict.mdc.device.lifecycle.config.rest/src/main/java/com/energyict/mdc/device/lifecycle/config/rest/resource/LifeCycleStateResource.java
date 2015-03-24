package com.energyict.mdc.device.lifecycle.config.rest.resource;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.lifecycle.config.Privileges;
import com.energyict.mdc.device.lifecycle.config.rest.response.LifeCycleStateInfo;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
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
    private final Thesaurus thesaurus;
    private final DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;
    private final ResourceHelper resourceHelper;

    @Inject
    public LifeCycleStateResource(Thesaurus thesaurus, DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService, ResourceHelper resourceHelper) {
        this.thesaurus = thesaurus;
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
        this.resourceHelper = resourceHelper;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE_LIFE_CYCLES})
    public PagedInfoList getStatesForDeviceLifecycle(@PathParam("id") Long id, @BeanParam QueryParameters queryParams) {
        List<LifeCycleStateInfo> states = resourceHelper.findDeviceLifeCycleByIdOrThrowException(id).getFiniteStateMachine().getStates()
                .stream()
                .map(LifeCycleStateInfo::new)
                .sorted(Comparator.comparing(state -> state.name)) // alphabetical sort
                .collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("deviceLifeCycleStates", states, queryParams);
    }

    @GET
    @Path("/{stateId}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE_LIFE_CYCLES})
    public Response getStateById(@PathParam("id") Long lifeCycleId, @PathParam("stateId") Long stateId, @BeanParam QueryParameters queryParams) {
        State state = resourceHelper.findStateByIdOrThrowException(resourceHelper.findDeviceLifeCycleByIdOrThrowException(lifeCycleId), stateId);
        return Response.ok(new LifeCycleStateInfo(state)).build();
    }
}
