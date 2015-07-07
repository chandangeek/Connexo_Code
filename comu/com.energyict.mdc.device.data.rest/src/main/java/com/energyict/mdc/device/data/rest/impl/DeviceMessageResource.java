package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.config.DeviceMessageEnablement;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HttpMethod;
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
@DeviceStatesRestricted(value = {DefaultState.DECOMMISSIONED}, methods = {HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE})
public class DeviceMessageResource {
    private final ResourceHelper resourceHelper;
    private final DeviceMessageInfoFactory deviceMessageInfoFactory;
    private final MdcPropertyUtils mdcPropertyUtils;
    private final DeviceMessageSpecificationService deviceMessageSpecificationService;
    private final ExceptionFactory exceptionFactory;
    private final DeviceMessageSpecInfoFactory deviceMessageSpecInfoFactory;

    @Inject
    public DeviceMessageResource(ResourceHelper resourceHelper, DeviceMessageInfoFactory deviceMessageInfoFactory, MdcPropertyUtils mdcPropertyUtils, DeviceMessageSpecificationService deviceMessageSpecificationService, ExceptionFactory exceptionFactory, DeviceMessageSpecInfoFactory deviceMessageSpecInfoFactory) {
        this.resourceHelper = resourceHelper;
        this.deviceMessageInfoFactory = deviceMessageInfoFactory;
        this.mdcPropertyUtils = mdcPropertyUtils;
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
        this.exceptionFactory = exceptionFactory;
        this.deviceMessageSpecInfoFactory = deviceMessageSpecInfoFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_DATA,
            com.energyict.mdc.device.config.security.Privileges.EXECUTE_DEVICE_MESSAGE_1,
            com.energyict.mdc.device.config.security.Privileges.EXECUTE_DEVICE_MESSAGE_2,
            com.energyict.mdc.device.config.security.Privileges.EXECUTE_DEVICE_MESSAGE_3,
            com.energyict.mdc.device.config.security.Privileges.EXECUTE_DEVICE_MESSAGE_4})
    public DeviceMessageInfos getDeviceCommands(@PathParam("mRID") String mrid, @BeanParam JsonQueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        List<DeviceMessageInfo> infos = device.getMessages().stream().
                // we do the explicit filtering because some categories should be hidden for the user
                filter(deviceMessage -> deviceMessageSpecificationService.filteredCategoriesForUserSelection().contains(deviceMessage.getSpecification().getCategory())).
                sorted(comparing(DeviceMessage::getReleaseDate, nullsLast(Comparator.<Instant>naturalOrder().reversed()))).
                map(deviceMessageInfoFactory::asInfo).
                collect(toList());

        List<DeviceMessageInfo> infosInPage = ListPager.of(infos).from(queryParameters).find();

        PagedInfoList deviceMessages = PagedInfoList.fromPagedList("deviceMessages", infosInPage, queryParameters);

        DeviceMessageInfos info = new DeviceMessageInfos();
        info.deviceMessages = deviceMessages.getInfos();
        info.hasCommandsWithPrivileges = hasCommandsWithPrivileges(device) ;
        info.total = deviceMessages.getTotal();

        return info;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({com.energyict.mdc.device.config.security.Privileges.EXECUTE_DEVICE_MESSAGE_1,
            com.energyict.mdc.device.config.security.Privileges.EXECUTE_DEVICE_MESSAGE_2,
            com.energyict.mdc.device.config.security.Privileges.EXECUTE_DEVICE_MESSAGE_3,
            com.energyict.mdc.device.config.security.Privileges.EXECUTE_DEVICE_MESSAGE_4})
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
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("{deviceMessageId}")
    @RolesAllowed({com.energyict.mdc.device.config.security.Privileges.EXECUTE_DEVICE_MESSAGE_1,
            com.energyict.mdc.device.config.security.Privileges.EXECUTE_DEVICE_MESSAGE_2,
            com.energyict.mdc.device.config.security.Privileges.EXECUTE_DEVICE_MESSAGE_3,
            com.energyict.mdc.device.config.security.Privileges.EXECUTE_DEVICE_MESSAGE_4})
    public DeviceMessageInfo updateDeviceMessage(@PathParam("mRID") String mrid, @PathParam("deviceMessageId") long deviceMessageId, DeviceMessageInfo deviceMessageInfo) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        DeviceMessage<?> deviceMessage = findDeviceMessageOrThrowException(device, deviceMessageId);
        deviceMessage.setReleaseDate(deviceMessageInfo.releaseDate);
        if (deviceMessageInfo.status!=null && MessageStatusAdapter.REVOKED.equals(deviceMessageInfo.status.value)) {
            deviceMessage.revoke();
        }
        deviceMessage.save();

        // refresh and return
        device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        deviceMessage = findDeviceMessageOrThrowException(device, deviceMessageId);
        return deviceMessageInfoFactory.asInfo(deviceMessage);
    }

    private DeviceMessage<?> findDeviceMessageOrThrowException(Device device, long deviceMessageId) {
        return device.getMessages().stream().filter(message -> message.getId() == deviceMessageId).findFirst().orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_SUCH_MESSAGE));
    }

    private boolean hasCommandsWithPrivileges (Device device) {
        final boolean[] hasCommandsWithPrivileges = {false};
        Set<DeviceMessageId> supportedMessagesSpecs = device.getDeviceType().getDeviceProtocolPluggableClass().getDeviceProtocol().getSupportedMessages();
        List<DeviceMessageId> enabledDeviceMessageIds = device.getDeviceConfiguration().getDeviceMessageEnablements().stream().map(DeviceMessageEnablement::getDeviceMessageId).collect(Collectors.toList());
        deviceMessageSpecificationService.filteredCategoriesForUserSelection().stream().sorted((c1,c2)->c1.getName().compareToIgnoreCase(c2.getName())).forEach(category-> {
            List<DeviceMessageSpecInfo> deviceMessageSpecs = category.getMessageSpecifications().stream()
                    .filter(deviceMessageSpec -> supportedMessagesSpecs.contains(deviceMessageSpec.getId())) // limit to device message specs supported by the protocol
                    .filter(dms -> enabledDeviceMessageIds.contains(dms.getId())) // limit to device message specs enabled on the config
                    .filter(dms -> device.getDeviceConfiguration().isAuthorized(dms.getId())) // limit to device message specs whom the user is authorized to
                    .map(dms -> deviceMessageSpecInfoFactory.asInfoWithMessagePropertySpecs(dms, device))
                    .collect(Collectors.toList());
            if (!deviceMessageSpecs.isEmpty()) {
                hasCommandsWithPrivileges[0] = true;
            }
        });
        return hasCommandsWithPrivileges[0];
    }
}
