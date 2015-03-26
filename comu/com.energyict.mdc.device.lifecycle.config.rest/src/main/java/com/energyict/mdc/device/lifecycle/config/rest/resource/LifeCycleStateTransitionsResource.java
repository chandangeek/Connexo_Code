package com.energyict.mdc.device.lifecycle.config.rest.resource;

import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.lifecycle.config.AuthorizedAction;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.lifecycle.config.Privileges;
import com.energyict.mdc.device.lifecycle.config.rest.response.AuthorizedActionInfoFactory;
import com.energyict.mdc.device.lifecycle.config.rest.response.AuthorizedActionInfo;

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
    private final DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;
    private final ResourceHelper resourceHelper;
    private final AuthorizedActionInfoFactory authorizedActionInfoFactory;

    @Inject
    public LifeCycleStateTransitionsResource(
            DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService,
            ResourceHelper resourceHelper,
            AuthorizedActionInfoFactory authorizedActionInfoFactory) {
        this.deviceLifeCycleConfigurationService = deviceLifeCycleConfigurationService;
        this.resourceHelper = resourceHelper;
        this.authorizedActionInfoFactory = authorizedActionInfoFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE_LIFE_CYCLES})
    public PagedInfoList getActionsForDeviceLifecycle(@PathParam("cycleId") Long lifeCycleId, @BeanParam QueryParameters queryParams) {
        DeviceLifeCycle deviceLifeCycle = resourceHelper.findDeviceLifeCycleByIdOrThrowException(lifeCycleId);
        List<AuthorizedActionInfo> transitions = deviceLifeCycle.getAuthorizedActions()
                .stream()
                .map(action -> authorizedActionInfoFactory.from(action))
                .sorted(Comparator.comparing(transition -> transition.name)) // alphabetical sort
                .collect(Collectors.toList());
        return PagedInfoList.fromPagedList("deviceLifeCycleTransitions", ListPager.of(transitions).from(queryParams).find(), queryParams);
    }

    @GET
    @Path("/{transitionId}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE_LIFE_CYCLES})
    public Response getAuthorizedActionById(@PathParam("cycleId") Long lifeCycleId, @PathParam("transitionId") Long transitionId, @BeanParam QueryParameters queryParams) {
        DeviceLifeCycle deviceLifeCycle = resourceHelper.findDeviceLifeCycleByIdOrThrowException(lifeCycleId);
        AuthorizedAction action = resourceHelper.findAuthorizedActionByIdOrThrowException(deviceLifeCycle, transitionId);
        return Response.ok(authorizedActionInfoFactory.from(action)).build();
    }
}
