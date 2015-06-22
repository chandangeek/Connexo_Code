package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.rest.IdWithNameInfo;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.ExecutableAction;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

public class DeviceLifeCycleActionResource {

    private final DeviceLifeCycleService deviceLifeCycleService;
    private final ResourceHelper resourceHelper;
    private final ExceptionFactory exceptionFactory;
    private final DeviceLifeCycleActionInfoFactory deviceLifeCycleActionInfoFactory;

    @Inject
    public DeviceLifeCycleActionResource(
            DeviceLifeCycleService deviceLifeCycleService,
            ResourceHelper resourceHelper,
            ExceptionFactory exceptionFactory,
            DeviceLifeCycleActionInfoFactory deviceLifeCycleActionInfoFactory) {
        this.deviceLifeCycleService = deviceLifeCycleService;
        this.resourceHelper = resourceHelper;
        this.exceptionFactory = exceptionFactory;
        this.deviceLifeCycleActionInfoFactory = deviceLifeCycleActionInfoFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.VIEW_DEVICE})
    public Response getAvailableActionsForCurrentDevice(@PathParam("mRID") String mrid, @BeanParam JsonQueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        List<IdWithNameInfo> availableActions = deviceLifeCycleService.getExecutableActions(device)
                .stream()
                .map(executableAction -> new IdWithNameInfo(executableAction.getAction()))
                .collect(Collectors.toList());
        return Response.ok(PagedInfoList.fromCompleteList("transitions", availableActions, queryParameters)).build();
    }


    @GET
    @Path("/{actionId}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.VIEW_DEVICE})
    public Response getPropertiesForAction(@PathParam("mRID") String mrid, @PathParam("actionId") long actionId, @BeanParam JsonQueryParameters queryParameters){
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        ExecutableAction requestedAction = deviceLifeCycleService.getExecutableActions(device)
                .stream()
                .filter(candidate -> candidate.getAction().getId() == actionId)
                .findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_DEVICE_LIFE_CYCLE_ACTION, actionId));
        DeviceLifeCycleActionInfo info = deviceLifeCycleActionInfoFactory.from(requestedAction);
        return Response.ok(info).build();
    }
}
