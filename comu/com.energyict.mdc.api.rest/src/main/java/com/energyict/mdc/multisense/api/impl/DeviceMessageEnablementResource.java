package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PROPFIND;
import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceMessageEnablement;
import com.energyict.mdc.device.config.DeviceMessageEnablementBuilder;
import com.energyict.mdc.multisense.api.impl.utils.FieldSelection;
import com.energyict.mdc.multisense.api.impl.utils.MessageSeeds;
import com.energyict.mdc.multisense.api.impl.utils.PagedInfoList;
import com.energyict.mdc.multisense.api.security.Privileges;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.Comparator;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Path("/devicetypes/{deviceTypeId}/deviceconfigurations/{deviceConfigId}/devicemessageenablements")
public class DeviceMessageEnablementResource {

    private final DeviceMessageEnablementInfoFactory deviceMessageEnablementInfoFactory;
    private final DeviceConfigurationService deviceConfigurationService;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public DeviceMessageEnablementResource(DeviceConfigurationService deviceConfigurationService, DeviceMessageEnablementInfoFactory deviceMessageEnablementInfoFactory, ExceptionFactory exceptionFactory) {
        this.deviceConfigurationService = deviceConfigurationService;
        this.deviceMessageEnablementInfoFactory = deviceMessageEnablementInfoFactory;
        this.exceptionFactory = exceptionFactory;
    }

    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @RolesAllowed(Privileges.Constants.PUBLIC_REST_API)
    @Path("/{deviceMessageEnablementId}")
    public DeviceMessageEnablementInfo getDeviceMessageEnablement(
            @PathParam("deviceTypeId") long deviceTypeId, @PathParam("deviceConfigId") long deviceConfigId,
            @PathParam("deviceMessageEnablementId") long deviceMessageEnablementId,
            @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo) {
        DeviceMessageEnablementInfo deviceMessageEnablement = deviceConfigurationService.
                findDeviceType(deviceTypeId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE_TYPE))
                .getConfigurations().stream().filter(dc -> dc.getId() == deviceConfigId)
                .findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE_CONFIG))
                .getDeviceMessageEnablements().stream()
                .filter(msg -> msg.getId() == deviceMessageEnablementId)
                .findFirst()
                .map(msg -> deviceMessageEnablementInfoFactory.from(msg, uriInfo, fieldSelection.getFields()))
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE_MESSAGE_ENABLEMENT));
        return deviceMessageEnablement;
    }

    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @RolesAllowed(Privileges.Constants.PUBLIC_REST_API)
    public PagedInfoList<DeviceMessageEnablementInfo> getDeviceMessageEnablements(
            @PathParam("deviceTypeId") long deviceTypeId, @PathParam("deviceConfigId") long deviceConfigId,
            @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo, @BeanParam JsonQueryParameters queryParameters) {
        List<DeviceMessageEnablement> deviceMessageEnablements = deviceConfigurationService.
                findDeviceType(deviceTypeId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE_TYPE))
                .getConfigurations().stream().filter(dc -> dc.getId() == deviceConfigId)
                .findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE_CONFIG))
                .getDeviceMessageEnablements();
        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
                .path(DeviceMessageEnablementResource.class)
                .resolveTemplate("deviceTypeId", deviceTypeId)
                .resolveTemplate("deviceConfigId", deviceConfigId);

        List<DeviceMessageEnablementInfo> infos = ListPager.of(deviceMessageEnablements, Comparator.comparingLong(DeviceMessageEnablement::getId))
                .from(queryParameters).stream().map(msg -> deviceMessageEnablementInfoFactory.from(msg, uriInfo, fieldSelection.getFields())).collect(toList());
        return PagedInfoList.from(infos, queryParameters, uriBuilder, uriInfo);
    }

    @PROPFIND
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @RolesAllowed(Privileges.Constants.PUBLIC_REST_API)
    public List<String> getFields() {
        return deviceMessageEnablementInfoFactory.getAvailableFields().stream().sorted().collect(toList());
    }

    @POST @Transactional
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @RolesAllowed(Privileges.Constants.PUBLIC_REST_API)
    public Response createDeviceMessageEnablement(
            @PathParam("deviceTypeId") long deviceTypeId, @PathParam("deviceConfigId") long deviceConfigId,
            DeviceMessageEnablementInfo info, @Context UriInfo uriInfo) {
        DeviceConfiguration deviceConfiguration = deviceConfigurationService.
                findDeviceType(deviceTypeId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE_TYPE))
                .getConfigurations().stream().filter(dc -> dc.getId() == deviceConfigId)
                .findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE_CONFIG));
        if (info.messageId==null) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.EXPECTED_MESSAGE_ID);
        }
        DeviceMessageEnablementBuilder builder = deviceConfiguration.createDeviceMessageEnablement(DeviceMessageId.havingId(info.messageId));
        if (info.userActions!=null && !info.userActions.isEmpty()) {
            info.userActions.stream().forEach(builder::addUserAction);
        }
        DeviceMessageEnablement deviceMessageEnablement = builder.build();
        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
                .path(DeviceMessageEnablementResource.class)
                .path(DeviceMessageEnablementResource.class, "getDeviceMessageEnablement")
                .resolveTemplate("deviceTypeId", deviceTypeId)
                .resolveTemplate("deviceConfigId", deviceConfigId)
                .resolveTemplate("deviceMessageEnablementId", deviceMessageEnablement);
        return Response.created(uriBuilder.build()).build();
    }

}
