package com.energyict.mdc.device.lifecycle.config.rest.resource;

import com.elster.jupiter.fsm.StateTransition;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.lifecycle.config.Privileges;
import com.energyict.mdc.device.lifecycle.config.rest.response.LifeCycleStateTransitionFactory;
import com.energyict.mdc.device.lifecycle.config.rest.response.LifeCycleStateTransitionInfo;

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

public class LifeCycleStateTransitionsResource {
    private final Thesaurus thesaurus;
    private final DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;
    private final ResourceHelper resourceHelper;
    private final LifeCycleStateTransitionFactory lifeCycleStateTransitionFactory;

    @Inject
    public LifeCycleStateTransitionsResource(
            Thesaurus thesaurus,
            DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService,
            ResourceHelper resourceHelper,
            LifeCycleStateTransitionFactory lifeCycleStateTransitionFactory) {
        this.thesaurus = thesaurus;
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
        this.resourceHelper = resourceHelper;
        this.lifeCycleStateTransitionFactory = lifeCycleStateTransitionFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE_LIFE_CYCLES})
    public PagedInfoList getTransitionsForDeviceLifecycle(@PathParam("cycleId") Long lifeCycleId, @BeanParam QueryParameters queryParams) {
        List<LifeCycleStateTransitionInfo> transitions = resourceHelper.findDeviceLifeCycleByIdOrThrowException(lifeCycleId).getFiniteStateMachine().getTransitions()
                .stream()
                .map(lifeCycleStateTransitionFactory::from)
                .sorted(Comparator.comparing(transition -> transition.name)) // alphabetical sort
                .collect(Collectors.toList());
        return PagedInfoList.fromCompleteList("deviceLifeCycleTransitions", transitions, queryParams);
    }

    @GET
    @Path("/{transitionId}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE_LIFE_CYCLES})
    public Response getStateTransitionById(@PathParam("cycleId") Long lifeCycleId, @PathParam("transitionId") Long transitionId, @BeanParam QueryParameters queryParams) {
        StateTransition transition = resourceHelper.findStateTransitionByIdOrThrowException(resourceHelper.findDeviceLifeCycleByIdOrThrowException(lifeCycleId), transitionId);
        return Response.ok(lifeCycleStateTransitionFactory.from(transition)).build();
    }
}
