package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.fsm.CustomStateTransitionEventType;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.imp.DeviceImportService;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.ExecutableAction;
import com.energyict.mdc.device.lifecycle.ExecutableActionProperty;
import com.energyict.mdc.device.lifecycle.config.AuthorizedTransitionAction;
import com.energyict.mdc.device.topology.TopologyService;
import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import static java.util.stream.Collectors.toList;

/**
 * Created by bvn on 4/22/15.
 */
@Path("/devices")
public class DeviceResource {

    private final DeviceService deviceService;
    private final DeviceInfoFactory deviceInfoFactory;
    private final DeviceConfigurationService deviceConfigurationService;
    private final DeviceImportService deviceImportService;
    private final ExceptionFactory exceptionFactory;
    private final TopologyService topologyService;
    private final FiniteStateMachineService finiteStateMachineService;
    private final DeviceLifeCycleService deviceLifeCycleService;
    private final Clock clock;


    @Inject
    public DeviceResource(DeviceService deviceService, DeviceInfoFactory deviceInfoFactory, DeviceConfigurationService deviceConfigurationService, DeviceImportService deviceImportService, ExceptionFactory exceptionFactory, TopologyService topologyService, FiniteStateMachineService finiteStateMachineService, DeviceLifeCycleService deviceLifeCycleService, Clock clock) {
        this.deviceService = deviceService;
        this.deviceInfoFactory = deviceInfoFactory;
        this.deviceConfigurationService = deviceConfigurationService;
        this.deviceImportService = deviceImportService;
        this.exceptionFactory = exceptionFactory;
        this.topologyService = topologyService;
        this.finiteStateMachineService = finiteStateMachineService;
        this.deviceLifeCycleService = deviceLifeCycleService;
        this.clock = clock;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_DATA})
    @Path("/{mrid}")
    public Response getDevice(@PathParam("mrid") String mRID, @BeanParam SelectedFields fields, @Context UriInfo uriInfo) {
        DeviceInfo deviceInfo = deviceService.findByUniqueMrid(mRID).map(d -> deviceInfoFactory.asHypermedia(d, uriInfo, fields.getFields())).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND.getStatusCode()));
        return Response.ok(deviceInfo).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_DATA})
    public Response getDevices(@BeanParam JsonQueryParameters queryParameters, @BeanParam SelectedFields fields, @Context UriInfo uriInfo) {
        List<DeviceInfo> infos = deviceService.findAllDevices(Condition.TRUE).from(queryParameters).stream().map(d -> deviceInfoFactory.asHypermedia(d, uriInfo, fields.getFields())).collect(toList());
        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder().path(DeviceResource.class);
        return Response.ok(PagedInfoList.from(infos, queryParameters, uriBuilder, uriInfo)).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_DATA})
    @Path("/{mrid}/actions")
    public Response getDeviceExecutableActions(@PathParam("mrid") String mRID,
                                               @BeanParam SelectedFields fields,
                                               @Context UriInfo uriInfo,
                                               @BeanParam JsonQueryParameters queryParameters) {
        Device device = deviceService.findByUniqueMrid(mRID).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND.getStatusCode()));
        List<DeviceLifeCycleActionInfo> infos = deviceLifeCycleService.getExecutableActions(device).stream().
                map(ExecutableAction::getAction).
                filter(aa -> aa instanceof AuthorizedTransitionAction).
                map(AuthorizedTransitionAction.class::cast).
                map(action -> deviceInfoFactory.createDeviceLifecycleActionInfo(device, action, uriInfo)).
                collect(toList());
        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder().
                path(DeviceResource.class).
                path(DeviceResource.class, "getDeviceExecutableActions").
                resolveTemplate("mrid", device.getmRID());
        return Response.ok(PagedInfoList.from(infos, queryParameters, uriBuilder, uriInfo)).build();
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_DATA})
    @Path("/{mrid}/actions/{actionId}")
    public Response executeAction(
                @PathParam("mrid") String mrid,
                @PathParam("actionId") long actionId,
                @BeanParam JsonQueryParameters queryParameters,
                DeviceLifeCycleActionInfo info){
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

        return Response.ok().build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.ADD_DEVICE)
    public Response createDevice(DeviceInfo info, @Context UriInfo uriInfo, @BeanParam SelectedFields fields) {
        Optional<DeviceConfiguration> deviceConfiguration = Optional.empty();
        if (info.deviceConfiguration != null && info.deviceConfiguration.id != null) {
            deviceConfiguration = deviceConfigurationService.findDeviceConfiguration(info.deviceConfiguration.id);
        }

        Device newDevice = deviceService.newDevice(deviceConfiguration.orElse(null), info.mRID, info.mRID);
        newDevice.setSerialNumber(info.serialNumber);
        newDevice.setYearOfCertification(info.yearOfCertification);
        newDevice.save();

        if (info.batch != null) {
            deviceImportService.addDeviceToBatch(newDevice, info.batch);
        }
        URI uri = uriInfo.getBaseUriBuilder().
                path(DeviceResource.class).
                path(DeviceResource.class, "getDevice").
                build(newDevice.getmRID());

        return Response.created(uri).build();
    }

    @PUT
    @Path("/{mrid}")
    @Consumes(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.ADMINISTRATE_DEVICE_COMMUNICATION)
    public Response updateDevice(@PathParam("mrid") String mrid, DeviceInfo info, @Context SecurityContext securityContext, @Context UriInfo uriInfo) {
        Device device = deviceService.findAndLockDeviceBymRIDAndVersion(mrid, info.version == null ? 0 : info.version).orElseThrow(() -> new WebApplicationException(Response.Status.CONFLICT));
        if (info.masterDevice!=null && info.masterDevice.mRID != null) {
            if (device.getDeviceConfiguration().isDirectlyAddressable()) {
                throw exceptionFactory.newException(MessageSeeds.IMPOSSIBLE_TO_SET_MASTER_DEVICE, device.getmRID());
            }
            Optional<Device> currentGateway = topologyService.getPhysicalGateway(device);
            if (!currentGateway.isPresent() || !currentGateway.get().getmRID().equals(info.masterDevice.mRID)) {
                Device newGateway = deviceService.findByUniqueMrid(info.masterDevice.mRID).orElseThrow(() -> new WebApplicationException("Unknown gateway mRID", Response.Status.BAD_REQUEST));
                topologyService.setPhysicalGateway(device, newGateway);
            }
        } else {
            if (topologyService.getPhysicalGateway(device).isPresent()) {
                topologyService.clearPhysicalGateway(device);
            }
        }
        device.setName(info.name);
        device.setSerialNumber(info.serialNumber);
        device.setYearOfCertification(info.yearOfCertification);
        if (info.lifecycleState!=null) {
            triggerStateTransition(device, info.lifecycleState);
        }

        device.save();
        return Response.ok(deviceInfoFactory.asHypermedia(device, uriInfo, Collections.emptyList())).build();
    }

    private void triggerStateTransition(Device device, String lifecycleState) {
        CustomStateTransitionEventType eventType = this.finiteStateMachineService.findCustomStateTransitionEventType(lifecycleState).orElseThrow(() -> new IllegalArgumentException("Custom state transition event type " + lifecycleState + " does not exist"));
        ExecutableAction executableAction = this.deviceLifeCycleService.getExecutableActions(device, eventType).orElseThrow(() -> new IllegalArgumentException("Current state of device with mRID " + device.getmRID() + " does not support the event type"));
        ((AuthorizedTransitionAction) executableAction).
                getActions().stream().
                flatMap(ma -> deviceLifeCycleService.getPropertySpecsFor(ma).stream()).
                        map(PropertySpec::getName).
                        collect(toList());

        executableAction.execute(Instant.now(), Collections.emptyList());
    }

    @DELETE
    @Path("/{mrid}")
    @RolesAllowed(Privileges.REMOVE_DEVICE)
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    public Response deleteDevice(@PathParam("mrid") String mrid) {
        Device device = deviceService.findByUniqueMrid(mrid).orElseThrow(() -> new WebApplicationException(Response.Status.CONFLICT));
        device.delete();
        return Response.ok().build();
    }

    @GET
    @Path("/fields")
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_DATA})
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    public Response getFields() {
        return Response.ok(deviceInfoFactory.getAvailableFields().stream().sorted().collect(toList())).build();
    }

    private List<ExecutableActionProperty> getExecutableActionPropertiesFromInfo(DeviceLifeCycleActionInfo info, AuthorizedTransitionAction authorizedAction) {
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

    private ExecutableAction getExecutableActionByIdOrThrowException(@PathParam("actionId") long actionId, Device device) {
        return deviceLifeCycleService.getExecutableActions(device)
                    .stream()
                    .filter(candidate -> candidate.getAction().getId() == actionId)
                    .findFirst()
                    .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_DEVICE_LIFE_CYCLE_ACTION, actionId));
    }

}

