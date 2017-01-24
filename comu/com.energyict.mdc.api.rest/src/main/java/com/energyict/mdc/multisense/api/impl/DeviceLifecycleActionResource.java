package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.rest.api.util.v1.hypermedia.FieldSelection;
import com.elster.jupiter.rest.api.util.v1.hypermedia.PagedInfoList;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.ExecutableAction;
import com.energyict.mdc.device.lifecycle.ExecutableActionProperty;
import com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction;
import com.energyict.mdc.multisense.api.impl.utils.MessageSeeds;
import com.energyict.mdc.multisense.api.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    /**
     * A device action models an action that can be authorized to initiate a StateTransition.
     *
     * @summary Fetch a device action
     *
     * @param mRID mRID of the device
     * @param actionId Id of the action
     * @param uriInfo uriInfo
     * @param fieldSelection field selection
     * @return Returns a unqiuely identofied device action
     */
    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    @Path("/{actionId}")
    public LifeCycleActionInfo getAction(@PathParam("mrid") String mRID,
                                         @PathParam("actionId") long actionId,
                                         @Context UriInfo uriInfo,
                                         @BeanParam FieldSelection fieldSelection) {
        Device device = deviceService.findDeviceByMrid(mRID)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE));
        ExecutableAction executableAction = getExecutableActionByIdOrThrowException(actionId, device);
        return deviceLifecycleActionInfoFactory.createDeviceLifecycleActionInfo(device, (AuthorizedTransitionAction) executableAction.getAction(), uriInfo, fieldSelection.getFields());
    }

    /**
     * A device action models an action that can be authorized to initiate a StateTransition.
     *
     * @summary Fetch a set of device actions
     *
     * @param mRID mRID of the device
     * @param uriInfo uriInfo
     * @param fieldSelection field selection
     * @param queryParameters queryParameters
     * @return a sorted, pageable list of elements. Only fields mentioned in field-param will be provided, or all fields if no
     * field-param was provided. The list will be sorted according to db order.
     */
    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public PagedInfoList<LifeCycleActionInfo> getDeviceExecutableActions(@PathParam("mrid") String mRID,
                                                    @BeanParam FieldSelection fieldSelection,
                                                    @Context UriInfo uriInfo,
                                                    @BeanParam JsonQueryParameters queryParameters) {
        Device device = deviceService.findDeviceByMrid(mRID)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE));
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

    /**
     * A device action models an action that can be authorized to initiate a StateTransition.
     * This PUT method will not actually create an action, but execute it.
     *
     * @summary Execute an action
     *
     * @param mrid mRID of the device
     * @param actionId Id of the action
     * @param queryParameters queryParameters
     * @param info Payload describing the parameters for the action execution
     * @return Returns OK(http 200) if the action was executed
     */
    @PUT @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    @Path("/{actionId}")
    public Response executeAction(
                @PathParam("mrid") String mrid,
                @PathParam("actionId") long actionId,
                @BeanParam JsonQueryParameters queryParameters,
                LifeCycleActionInfo info){
        if (info==null) {
            throw exceptionFactory.newException(MessageSeeds.CONTENT_EXPECTED);
        }
        if (info.device == null || info.device.version == null) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.VERSION_MISSING, "device");
        }
        Device device = deviceService.findDeviceByMrid(mrid).orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE));
        device = deviceService.findAndLockDeviceByIdAndVersion(device.getId(), info.device.version)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.CONFLICT, MessageSeeds.CONFLICT_ON_DEVICE));
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
