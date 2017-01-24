package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.rest.api.util.v1.hypermedia.FieldSelection;
import com.elster.jupiter.rest.api.util.v1.hypermedia.PagedInfoList;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PROPFIND;
import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.multisense.api.impl.utils.MessageSeeds;
import com.energyict.mdc.multisense.api.security.Privileges;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Path("/devices/{mrid}/messages")
public class DeviceMessageResource {

    private final DeviceMessageInfoFactory deviceMessageInfoFactory;
    private final DeviceService deviceService;
    private final ExceptionFactory exceptionFactory;
    private final DeviceMessageSpecificationService deviceMessageSpecificationService;
    private final MdcPropertyUtils mdcPropertyUtils;
    private final DeviceMessageService deviceMessageService;

    @Inject
    public DeviceMessageResource(DeviceService deviceService, DeviceMessageInfoFactory deviceMessageInfoFactory, ExceptionFactory exceptionFactory,
                                 DeviceMessageSpecificationService deviceMessageSpecificationService, MdcPropertyUtils mdcPropertyUtils,
                                 DeviceMessageService deviceMessageService) {
        this.deviceService = deviceService;
        this.deviceMessageInfoFactory = deviceMessageInfoFactory;
        this.exceptionFactory = exceptionFactory;
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
        this.mdcPropertyUtils = mdcPropertyUtils;
        this.deviceMessageService = deviceMessageService;
    }

    /**
     * Device message is used for infrequent adjustments of device parameters such as resetting a device. Device messages are only
     * available when they are specified in a device protocol and this protocol is applied to the device type.
     *
     * Device messages are used to remotely adjust parameters of a device on a one-time basis. To make device messages
     * available in the menu of a device, a device protocol must be applied to the device’s device type. Device messages
     * also have a release date on which they become available (=pending) for sending to the device. Finally, the user
     * must have sufficient privileges to be able to send device messages. This is specified in the roles management.
     *
     * Devices are split into categories. For each category and for each individual message the availability and execution
     * level can be configured.
     *
     * The device interprets this message when it is communicated via the communication task and carries out the
     * requested adjustments of the parameters.
     *
     * An external system or eiServer’s own communication tasks will communicate device messages to the device allowing
     * them to execute the actions specified in their definition. The command Messages is used for this purpose. If it
     * is not picked up by a communication task, it will simply remain in the system without impact.
     *
     * @summary Retrieve a single device message
     *
     * @param mRID The device's mRID
     * @param messageId The device message identifier
     * @param uriInfo uriInfo
     * @param fieldSelection field selection
     * @return Uniquely identified device message
     */
    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @Path("/{messageId}")
    @RolesAllowed(Privileges.Constants.PUBLIC_REST_API)
    public DeviceMessageInfo getDeviceMessage(@PathParam("mrid") String mRID, @PathParam("messageId") long messageId,
                                              @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo) {
        Device device = deviceService.findDeviceByMrid(mRID).orElseThrow(exceptionFactory
                .newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE));
        DeviceMessage deviceMessage = device.getMessages().stream().filter(msg -> msg.getId() == messageId).findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_DEVICE_MESSAGE));

        return deviceMessageInfoFactory.from(deviceMessage, uriInfo, fieldSelection.getFields());
    }


    /**
     * Device message is used for infrequent adjustments of device parameters such as resetting a device. Device messages are only
     * available when they are specified in a device protocol and this protocol is applied to the device type.
     *
     * Device messages are used to remotely adjust parameters of a device on a one-time basis. To make device messages
     * available in the menu of a device, a device protocol must be applied to the device’s device type. Device messages
     * also have a release date on which they become available (=pending) for sending to the device. Finally, the user
     * must have sufficient privileges to be able to send device messages. This is specified in the roles management.
     *
     * Devices are split into categories. For each category and for each individual message the availability and execution
     * level can be configured.
     *
     * The device interprets this message when it is communicated via the communication task and carries out the
     * requested adjustments of the parameters.
     *
     * An external system or eiServer’s own communication tasks will communicate device messages to the device allowing
     * them to execute the actions specified in their definition. The command Messages is used for this purpose. If it
     * is not picked up by a communication task, it will simply remain in the system without impact.
     *
     * @summary Retrieve all known device messages for a certain device
     *
     * @param mRID The device's mRID
     * @param uriInfo uriInfo
     * @param fieldSelection field selection
     * @param queryParameters queryParameters
     * @return a sorted, pageable list of elements. Only fields mentioned in field-param will be provided, or all fields if no
     * field-param was provided. The list will be sorted according to db order.
     */
    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @RolesAllowed(Privileges.Constants.PUBLIC_REST_API)
    public PagedInfoList<DeviceMessageInfo> getDeviceMessages(@PathParam("mrid") String mRID,
                                                              @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo,
                                                              @BeanParam JsonQueryParameters queryParameters) {
        List<DeviceMessageInfo> infos = deviceService.findDeviceByMrid(mRID)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE))
                .getMessages().stream()
                .map(ct -> deviceMessageInfoFactory.from(ct, uriInfo, fieldSelection.getFields()))
                .collect(toList());
        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
                .path(DeviceMessageResource.class)
		        .resolveTemplate("mrid", mRID);
        return PagedInfoList.from(infos, queryParameters, uriBuilder, uriInfo);
    }

    /**
     * No check will be done to verify the device message will actually be send to the device. There might not be a
     * communication task that picks up your message. If the newly created message contains a reference 'preferredComTask',
     * the referenced ComTask can be triggered with 'runnow' for pickup of the message.
     *
     * @summary Create a new device message for a device
     *
     * @param mRID The device's mRID
     * @param deviceMessageInfo payload describing the values for the to-be-created device messsage
     * @param uriInfo uriInfo
     * @return url to newly created device message
     * @responseheader location href to newly created device message
     */
    @POST @Transactional
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @RolesAllowed(Privileges.Constants.PUBLIC_REST_API)
    public Response createDeviceMessage(@PathParam("mrid") String mRID, DeviceMessageInfo deviceMessageInfo, @Context UriInfo uriInfo) {
        if (deviceMessageInfo.device==null || deviceMessageInfo.device.version==null) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.VERSION_MISSING, "device.version");
        }
        if (deviceMessageInfo.messageSpecification==null || deviceMessageInfo.messageSpecification.id==null) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.EXPECTED_MESSAGE_SPEC_ID);
        }
        if (deviceMessageInfo.releaseDate==null) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.EXPECTED_RELEASE_DATE);
        }

        Device device = deviceService.findAndLockDeviceBymRIDAndVersion(mRID, deviceMessageInfo.device.version)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.CONFLICT, MessageSeeds.NO_SUCH_DEVICE));
        DeviceMessageId deviceMessageId = DeviceMessageId.havingId(deviceMessageInfo.messageSpecification.id);
        Device.DeviceMessageBuilder deviceMessageBuilder = device.newDeviceMessage(deviceMessageId).setReleaseDate(deviceMessageInfo.releaseDate);
        DeviceMessageSpec deviceMessageSpec = deviceMessageSpecificationService
                .findMessageSpecById(deviceMessageId.dbValue())
                .orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_SUCH_DEVICE_MESSAGE_SPEC));

        if (deviceMessageInfo.deviceMessageAttributes !=null) {
            try {
                for (PropertySpec propertySpec : deviceMessageSpec.getPropertySpecs()) {
                    Object propertyValue = mdcPropertyUtils.findPropertyValue(propertySpec, deviceMessageInfo.deviceMessageAttributes);
                    if (propertyValue != null) {
                        deviceMessageBuilder.addProperty(propertySpec.getName(), propertyValue);
                    }
                }
            } catch (LocalizedFieldValidationException e) {
                throw new LocalizedFieldValidationException(e.getMessageSeed(), "properties."+e.getViolatingProperty());
            }
        }
        DeviceMessage deviceDeviceMessage = deviceMessageBuilder.add();
        URI uri = uriInfo.getBaseUriBuilder()
                .path(DeviceMessageResource.class)
                .path(DeviceMessageResource.class, "getDeviceMessage")
                .build(device.getmRID(), deviceDeviceMessage.getId());
        return Response.created(uri).build();
    }

    /**
     * Currently only protocol information and release date can be changed. All other fields will be ignored.
     *
     * @summary Update and exiting device message.
     *
     * @param mRID The device's mRID
     * @param messageId The device message identifier
     * @param uriInfo uriInfo
     * @param deviceMessageInfo Values to update the device message
     * @return Device message
     */
    @PUT @Transactional
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @RolesAllowed(Privileges.Constants.PUBLIC_REST_API)
    @Path("/{messageId}")
    public DeviceMessageInfo updateDeviceMessage(@PathParam("mrid") String mRID, @PathParam("messageId") long messageId,
                                                 DeviceMessageInfo deviceMessageInfo, @Context UriInfo uriInfo) {
        if (deviceMessageInfo.version==null) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.VERSION_MISSING, "version");
        }
        if (deviceMessageInfo.device==null || deviceMessageInfo.device.version==null) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.VERSION_MISSING, "device.version");
        }
        if (deviceMessageInfo.messageSpecification==null || deviceMessageInfo.messageSpecification.id==null) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.EXPECTED_MESSAGE_SPEC_ID);
        }
        if (deviceMessageInfo.releaseDate==null) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.EXPECTED_RELEASE_DATE);
        }
        if (deviceMessageInfo.protocolInfo==null) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.EXPECTED_PROTOCOL_INFO);
        }
        Device device = deviceService.findAndLockDeviceBymRIDAndVersion(mRID, deviceMessageInfo.device.version)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.CONFLICT, MessageSeeds.NO_SUCH_DEVICE));
        DeviceMessage deviceMessage = deviceMessageService.findAndLockDeviceMessageByIdAndVersion(messageId, deviceMessageInfo.version)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.CONFLICT, MessageSeeds.NO_SUCH_DEVICE_MESSAGE));
        if (((Device) deviceMessage.getDevice()).getId()!=device.getId()) {     //Downcast to Connexo Device
            throw exceptionFactory.newException(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE_MESSAGE);
        }

        deviceMessage.setProtocolInformation(deviceMessageInfo.protocolInfo);
        deviceMessage.setReleaseDate(deviceMessageInfo.releaseDate);
        deviceMessage.save();

        DeviceMessage reloadedDeviceMessage = deviceMessageService.findDeviceMessageById(deviceMessage.getId()).get();

        return deviceMessageInfoFactory.from(reloadedDeviceMessage, uriInfo, Collections.emptyList());
    }


    /**
     * This delete will return the revoked message, because the message has not really been deleted, merely revoked.
     *
     * @summary Revokes a specific device message
     *
     * @param mRID The device's mRID
     * @param messageId The device message identifier
     * @param uriInfo uriInfo
     * @return Revoked device message
     * @statuscode 200 If the device message was revoked
     */
    @DELETE @Transactional
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @RolesAllowed(Privileges.Constants.PUBLIC_REST_API)
    @Path("/{messageId}")
    public DeviceMessageInfo deleteDeviceMessage(@PathParam("mrid") String mRID, @PathParam("messageId") long messageId,
                                                 @Context UriInfo uriInfo) {
        Device device = deviceService.findDeviceByMrid(mRID)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE));
        DeviceMessage deviceMessage = device.getMessages().stream().filter(msg -> msg.getId() == messageId).findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_DEVICE_MESSAGE));
        deviceMessage.revoke();
        DeviceMessage reloadedDeviceMessage = deviceMessageService.findDeviceMessageById(deviceMessage.getId()).get();
        return deviceMessageInfoFactory.from(reloadedDeviceMessage, uriInfo, Collections.emptyList());
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
    @RolesAllowed(Privileges.Constants.PUBLIC_REST_API)
    public List<String> getFields() {
        return deviceMessageInfoFactory.getAvailableFields().stream().sorted().collect(toList());
    }

}
