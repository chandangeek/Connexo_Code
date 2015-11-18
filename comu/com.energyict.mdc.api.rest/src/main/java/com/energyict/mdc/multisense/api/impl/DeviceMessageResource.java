package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PROPFIND;
import com.energyict.mdc.common.rest.Transactional;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.multisense.api.impl.utils.FieldSelection;
import com.energyict.mdc.multisense.api.impl.utils.MessageSeeds;
import com.energyict.mdc.multisense.api.impl.utils.PagedInfoList;
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

    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @Path("/{messageId}")
    @RolesAllowed(Privileges.Constants.PUBLIC_REST_API)
    public Response getDeviceMessage(@PathParam("mrid") String mRID, @PathParam("messageId") long messageId,
                                     @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo) {
        Device device = deviceService.findByUniqueMrid(mRID).orElseThrow(exceptionFactory
                .newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE));
        DeviceMessage<Device> deviceMessage = device.getMessages().stream().filter(msg -> msg.getId() == messageId).findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_DEVICE_MESSAGE));

        return Response.ok(deviceMessageInfoFactory.from(deviceMessage, uriInfo, fieldSelection.getFields())).build();
    }


    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @RolesAllowed(Privileges.Constants.PUBLIC_REST_API)
    public PagedInfoList<DeviceMessageInfo> getDeviceMessages(@PathParam("mrid") String mRID,
                                                              @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo,
                                                              @BeanParam JsonQueryParameters queryParameters) {

        List<DeviceMessageInfo> infos = deviceService.findByUniqueMrid(mRID)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE))
                .getMessages().stream()
                .map(ct -> deviceMessageInfoFactory.from(ct, uriInfo, fieldSelection.getFields()))
                .collect(toList());
        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
                .path(DeviceMessageResource.class)
		        .resolveTemplate("mrid", mRID);
        return PagedInfoList.from(infos, queryParameters, uriBuilder, uriInfo);
    }

    @POST @Transactional
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @RolesAllowed(Privileges.Constants.PUBLIC_REST_API)
    public Response createDeviceMessage(@PathParam("mrid") String mrid, DeviceMessageInfo deviceMessageInfo, @Context UriInfo uriInfo) {
        Device device = deviceService.findByUniqueMrid(mrid)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE));
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
        DeviceMessage<Device> deviceDeviceMessage = deviceMessageBuilder.add();
        URI uri = uriInfo.getBaseUriBuilder()
                .path(DeviceMessageResource.class)
                .path(DeviceMessageResource.class, "getDeviceMessage")
                .build(device.getmRID(), deviceDeviceMessage.getId());
        return Response.created(uri).build();
    }

    @PUT @Transactional
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @RolesAllowed(Privileges.Constants.PUBLIC_REST_API)
    @Path("/{messageId}")
    public Response updateDeviceMessage(@PathParam("mrid") String mrid, @PathParam("messageId") long messageId,
                                        DeviceMessageInfo deviceMessageInfo, @Context UriInfo uriInfo) {
        Device device = deviceService.findByUniqueMrid(mrid)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE));
        DeviceMessage<Device> deviceMessage = device.getMessages().stream().filter(msg -> msg.getId() == messageId).findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_DEVICE_MESSAGE));

        deviceMessage.setProtocolInformation(deviceMessageInfo.protocolInfo);
        deviceMessage.setReleaseDate(deviceMessageInfo.releaseDate);
        deviceMessage.save();

        DeviceMessage reloadedDeviceMessage = deviceMessageService.findDeviceMessageById(deviceMessage.getId()).get();

        return Response.ok().entity(deviceMessageInfoFactory.from(reloadedDeviceMessage, uriInfo, Collections.emptyList())).build();
    }

    @DELETE @Transactional
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @RolesAllowed(Privileges.Constants.PUBLIC_REST_API)
    @Path("/{messageId}")
    public Response deleteDeviceMessage(@PathParam("mrid") String mrid, @PathParam("messageId") long messageId,
                                        DeviceMessageInfo deviceMessageInfo, @Context UriInfo uriInfo) {
        Device device = deviceService.findByUniqueMrid(mrid)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE));
        DeviceMessage<Device> deviceMessage = device.getMessages().stream().filter(msg -> msg.getId() == messageId).findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_DEVICE_MESSAGE));

        deviceMessage.revoke();
        deviceMessage.save();
        DeviceMessage reloadedDeviceMessage = deviceMessageService.findDeviceMessageById(deviceMessage.getId()).get();
        return Response.ok().entity(deviceMessageInfoFactory.from(reloadedDeviceMessage, uriInfo, Collections.emptyList())).build();
    }



    @PROPFIND
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @RolesAllowed(Privileges.Constants.PUBLIC_REST_API)
    public List<String> getFields() {
        return deviceMessageInfoFactory.getAvailableFields().stream().sorted().collect(toList());
    }

}
