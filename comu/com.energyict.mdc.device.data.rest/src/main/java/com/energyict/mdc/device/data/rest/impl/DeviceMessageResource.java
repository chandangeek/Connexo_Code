package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static java.util.Comparator.comparing;
import static java.util.Comparator.nullsLast;
import static java.util.stream.Collectors.toList;

/**
 * Created by bvn on 10/22/14.
 */
public class DeviceMessageResource {
    private final ResourceHelper resourceHelper;
    private final DeviceMessageInfoFactory deviceMessageInfoFactory;
    private final MdcPropertyUtils mdcPropertyUtils;
    private final DeviceMessageSpecificationService deviceMessageSpecificationService;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public DeviceMessageResource(ResourceHelper resourceHelper, DeviceMessageInfoFactory deviceMessageInfoFactory, MdcPropertyUtils mdcPropertyUtils, DeviceMessageSpecificationService deviceMessageSpecificationService, ExceptionFactory exceptionFactory) {
        this.resourceHelper = resourceHelper;
        this.deviceMessageInfoFactory = deviceMessageInfoFactory;
        this.mdcPropertyUtils = mdcPropertyUtils;
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
        this.exceptionFactory = exceptionFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public PagedInfoList getDeviceCommands(@PathParam("mRID") String mrid, @BeanParam QueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        List<DeviceMessageInfo> infos = device.getMessages().stream().
                sorted(comparing(DeviceMessage::getReleaseDate, nullsLast(Comparator.<Instant>naturalOrder().reversed()))).
                map(deviceMessageInfoFactory::asInfo).
                collect(toList());

        List<DeviceMessageInfo> infosInPage = ListPager.of(infos).from(queryParameters).find();

        PagedInfoList deviceMessages = PagedInfoList.asJson("deviceMessages", infosInPage, queryParameters);
        return deviceMessages;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createDeviceMessage(@PathParam("mRID") String mrid, DeviceMessageInfo deviceMessageInfo) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        DeviceMessageId deviceMessageId = DeviceMessageId.valueOf(deviceMessageInfo.messageSpecification.id);
        Device.DeviceMessageBuilder deviceMessageBuilder = device.newDeviceMessage(deviceMessageId).setReleaseDate(deviceMessageInfo.releaseDate);
        DeviceMessageSpec deviceMessageSpec = deviceMessageSpecificationService.findMessageSpecById(deviceMessageId.dbValue()).orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_SUCH_MESSAGE_SPEC));

        if (deviceMessageInfo.properties !=null) {
            try {
                for (PropertySpec propertySpec : deviceMessageSpec.getPropertySpecs()) {
                    Object propertyValue = mdcPropertyUtils.findPropertyValue(propertySpec, deviceMessageInfo.properties);
                    if (propertyValue != null) {
                        deviceMessageBuilder.addProperty(propertySpec.getName(), propertyValue);
                    }
                }
            } catch (LocalizedFieldValidationException e) {
                throw new LocalizedFieldValidationException(e.getMessageSeed(), "properties."+e.getViolatingProperty());
            }
        }

        return Response.status(Response.Status.CREATED).entity(deviceMessageInfoFactory.asInfo(deviceMessageBuilder.add())).build();
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("{deviceMessageId}")
    public DeviceMessageInfo updateDeviceMessage(@PathParam("mRID") String mrid, @PathParam("deviceMessageId") long deviceMessageId, DeviceMessageInfo deviceMessageInfo) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        DeviceMessage<?> deviceMessage = findDeviceMessageOrThrowException(device, deviceMessageId);
        deviceMessage.setReleaseDate(deviceMessageInfo.releaseDate);
        deviceMessage.save();

        // refresh and return
        device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        deviceMessage = findDeviceMessageOrThrowException(device, deviceMessageId);
        return deviceMessageInfoFactory.asInfo(deviceMessage);
    }

    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("{deviceMessageId}")
    public Response deleteDeviceMessage(@PathParam("mRID") String mrid, @PathParam("deviceMessageId") long deviceMessageId) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        DeviceMessage<?> deviceMessage = findDeviceMessageOrThrowException(device, deviceMessageId);
        deviceMessage.revoke();
        deviceMessage.save();
        return Response.status(Response.Status.OK).build();
    }

    private DeviceMessage<?> findDeviceMessageOrThrowException(Device device, long deviceMessageId) {
        return device.getMessages().stream().filter(message -> message.getId() == deviceMessageId).findFirst().orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_SUCH_MESSAGE));
    }
}
