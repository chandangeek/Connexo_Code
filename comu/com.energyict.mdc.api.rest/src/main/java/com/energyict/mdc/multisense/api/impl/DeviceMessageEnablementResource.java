package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.api.util.v1.hypermedia.FieldSelection;
import com.elster.jupiter.rest.api.util.v1.hypermedia.PagedInfoList;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PROPFIND;
import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceMessageEnablement;
import com.energyict.mdc.device.config.DeviceMessageEnablementBuilder;
import com.energyict.mdc.multisense.api.impl.utils.MessageSeeds;
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

    /**
     * a device message enablement enables the usage of a device message
     * or an entire device message category
     * on a device configuration.
     *
     * @summary Fetch a uniquely identified device message enablement
     *
     * @param deviceTypeId Id of the device type
     * @param deviceConfigId Id of the device configuration
     * @param deviceMessageEnablementId  Id of the device message enablement
     * @param uriInfo uriInfo
     * @param fieldSelection field selection
     * @return Uniquely identified device message enablement
     */
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

    /**
     * a device message enablement enables the usage of a device message
     * or an entire device message category
     * on a device configuration.
     *
     * @summary Fetch a set of device message enablements
     *
     * @param deviceTypeId Id of the device type
     * @param deviceConfigId Id of the device configuration
     * @param uriInfo uriInfo
     * @param fieldSelection field selection
     * @param queryParameters queryParameters
     * @return a sorted, pageable list of elements. Only fields mentioned in field-param will be provided, or all fields if no
     * field-param was provided. The list will be sorted according to db order.
     */
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

    /**
     * List the fields available on this type of entity.
     * <br>E.g.
     * <br>[
     * <br> "id",
     * <br> "name",
     * <br> "actions",
     * <br> "batch"
     * <br>]
     * <br>Fields in the list can be used as parameter on a GET request to the same resource, e.g.
     * <br> <i></i>GET ..../resource?fields=id,name,batch</i>
     * <br> The call above will return only the requested fields of the entity. In the absence of a field list, all fields
     * will be returned. If IDs are required in the URL for parent entities, then will be ignored when using the PROPFIND method.
     *
     * @summary List the fields available on this type of entity
     * @return A list of field names that can be requested as parameter in the GET method on this entity type
     */
    @PROPFIND
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @RolesAllowed(Privileges.Constants.PUBLIC_REST_API)
    public List<String> getFields() {
        return deviceMessageEnablementInfoFactory.getAvailableFields().stream().sorted().collect(toList());
    }

    /**
     * a device message enablement enables the usage of a device message
     * or an entire device message category
     * on a device configuration.
     * <br>
     * By enabling a device message on a device configuration, the message becomes available to device of that config.
     *
     * @summary Enable a device message on a device configuration
     *
     * @param deviceTypeId Id of the device type
     * @param deviceConfigId Id of the device configuration
     * @param info Values for the to-be-created device message enablement
     * @param uriInfo uriInfo
     * @return url to newly created device message enablement
     * @responseheader location href to newly created device message enablement
     */
    @POST @Transactional
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @RolesAllowed(Privileges.Constants.PUBLIC_REST_API)
    public Response createDeviceMessageEnablement(
            @PathParam("deviceTypeId") long deviceTypeId, @PathParam("deviceConfigId") long deviceConfigId,
            DeviceMessageEnablementInfo info, @Context UriInfo uriInfo) {
        if (info.deviceConfiguration==null || info.deviceConfiguration.version==null) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.VERSION_MISSING, "deviceConfiguration.version");
        }
        if (info.deviceConfiguration.deviceType==null || info.deviceConfiguration.deviceType.version==null) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.VERSION_MISSING, "deviceConfiguration.deviceType.version");
        }
        deviceConfigurationService.
                findAndLockDeviceType(deviceTypeId, info.deviceConfiguration.deviceType.version)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.CONFLICT, MessageSeeds.NO_SUCH_DEVICE_TYPE));

        DeviceConfiguration deviceConfiguration = deviceConfigurationService.findAndLockDeviceConfigurationByIdAndVersion(deviceConfigId, info.deviceConfiguration.version)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.CONFLICT, MessageSeeds.NO_SUCH_DEVICE_CONFIG));
        if (deviceConfiguration.getDeviceType().getId()!=deviceTypeId) {
            throw exceptionFactory.newException(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE_CONFIG);
        }
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
