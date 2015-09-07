package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.ExecutableAction;
import com.energyict.mdc.device.lifecycle.ExecutableActionProperty;
import com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction;
import com.energyict.mdc.multisense.api.impl.utils.FieldSelection;
import com.energyict.mdc.multisense.api.impl.utils.MessageSeeds;
import com.energyict.mdc.multisense.api.impl.utils.PagedInfoList;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import static java.util.stream.Collectors.toList;

/**
 * Created by bvn on 7/9/15.
 */
@Path("/devices/{mrid}/actions")
public class DeviceLifecycleActionResource {

    private final DeviceService deviceService;
    private final DeviceLifecycleActionInfoFactory deviceLifecycleActionInfoFactory;
    private final ExceptionFactory exceptionFactory;
    private final DeviceLifeCycleService deviceLifeCycleService;
    private final Clock clock;

    @Inject
    public DeviceLifecycleActionResource(DeviceService deviceService, DeviceLifecycleActionInfoFactory deviceLifecycleActionInfoFactory, ExceptionFactory exceptionFactory, DeviceLifeCycleService deviceLifeCycleService, Clock clock) {
        this.deviceService = deviceService;
        this.deviceLifecycleActionInfoFactory = deviceLifecycleActionInfoFactory;
        this.exceptionFactory = exceptionFactory;
        this.deviceLifeCycleService = deviceLifeCycleService;
        this.clock = clock;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    @Path("/{actionId}")
    public LifeCycleActionInfo getAction(@PathParam("mrid") String mRID,
                                         @PathParam("actionId") long actionId,
                                         @Context UriInfo uriInfo,
                                         @BeanParam FieldSelection fieldSelection) {

        Device device = deviceService.findByUniqueMrid(mRID).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND.getStatusCode()));
        ExecutableAction executableAction = getExecutableActionByIdOrThrowException(actionId, device);
        return deviceLifecycleActionInfoFactory.createDeviceLifecycleActionInfo(device, (AuthorizedTransitionAction) executableAction.getAction(), uriInfo, fieldSelection.getFields());
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public PagedInfoList<LifeCycleActionInfo> getDeviceExecutableActions(@PathParam("mrid") String mRID,
                                                    @BeanParam FieldSelection fieldSelection,
                                                    @Context UriInfo uriInfo,
                                                    @BeanParam JsonQueryParameters queryParameters) {
        Device device = deviceService.findByUniqueMrid(mRID).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND.getStatusCode()));
        List<LifeCycleActionInfo> infos = deviceLifeCycleService.getExecutableActions(device).stream().
                map(ExecutableAction::getAction).
                filter(aa -> aa instanceof AuthorizedTransitionAction).
                map(AuthorizedTransitionAction.class::cast).
                map(action -> deviceLifecycleActionInfoFactory.createDeviceLifecycleActionInfo(device, action, uriInfo, fieldSelection.getFields())).
                collect(toList());
        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder().
                path(DeviceLifecycleActionResource.class).
                resolveTemplate("mrid", device.getmRID());
        return PagedInfoList.from(infos, queryParameters, uriBuilder, uriInfo);
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    @Path("/{actionId}")
    public Response executeAction(
                @PathParam("mrid") String mrid,
                @PathParam("actionId") long actionId,
                @BeanParam JsonQueryParameters queryParameters,
                LifeCycleActionInfo info){
        Device device = deviceService.findByUniqueMrid(mrid).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND.getStatusCode()));
        if (info==null) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        device = deviceService.findAndLockDeviceByIdAndVersion(device.getId(), info.deviceVersion).orElseThrow(() -> new WebApplicationException(Response.Status.CONFLICT));
        ExecutableAction requestedAction = getExecutableActionByIdOrThrowException(actionId, device);
        if (requestedAction.getAction() instanceof AuthorizedTransitionAction){
            requestedAction.execute(info.effectiveTimestamp==null?clock.instant():info.effectiveTimestamp, getExecutableActionPropertiesFromInfo(info, (AuthorizedTransitionAction) requestedAction.getAction()));
        } else {
            throw exceptionFactory.newException(MessageSeeds.CAN_NOT_HANDLE_ACTION);
        }

        return Response.ok().build(); // an action was called: nothing relevant to return
    }

    private List<ExecutableActionProperty> getExecutableActionPropertiesFromInfo(LifeCycleActionInfo info, AuthorizedTransitionAction authorizedAction) {
        if (info.properties==null) {
            return Collections.emptyList();
        }

        Map<String, PropertySpec> allPropertySpecsForAction = authorizedAction.getActions()
                .stream()
                .flatMap(microAction -> deviceLifeCycleService.getPropertySpecsFor(microAction).stream())
                .collect(Collectors.toMap(PropertySpec::getName, Function.<PropertySpec>identity()));

        List<ExecutableActionProperty> executableProperties = new ArrayList<>(allPropertySpecsForAction.size());

        for (PropertyInfo property : info.properties) {
            PropertySpec propertySpec = allPropertySpecsForAction.get(property.key);
            if (propertySpec != null && property.propertyValueInfo != null){
                try {
                    Object value = null;
                    if (property.propertyValueInfo.value != null) {
                        value = propertySpec.getValueFactory().fromStringValue(String.valueOf(property.propertyValueInfo.value));
                    }
                    executableProperties.add(deviceLifeCycleService.toExecutableActionProperty(value, propertySpec));
                } catch (InvalidValueException e) {
                    // Enable form validation
                    throw new LocalizedFieldValidationException(MessageSeeds.THIS_FIELD_IS_REQUIRED, propertySpec.getName());
                }
            }
        }
        return executableProperties;
    }

    private ExecutableAction getExecutableActionByIdOrThrowException(long actionId, Device device) {
        return deviceLifeCycleService.getExecutableActions(device)
                    .stream()
                    .filter(candidate -> candidate.getAction().getId() == actionId)
                    .findFirst()
                    .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_DEVICE_LIFE_CYCLE_ACTION, actionId));
    }



}
