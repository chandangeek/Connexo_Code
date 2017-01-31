/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.PROPFIND;
import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionBuilder;
import com.energyict.mdc.multisense.api.impl.utils.MessageSeeds;
import com.energyict.mdc.multisense.api.security.Privileges;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.tasks.MessagesTask;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.time.Clock;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

/**
 * Created by bvn on 9/16/15.
 */
@Path("/devices/{mrid}/contacter")
public class DeviceContactorResource {

    private final DeviceService deviceService;
    private final Clock clock;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public DeviceContactorResource(DeviceService deviceService, Clock clock, ExceptionFactory exceptionFactory) {
        this.deviceService = deviceService;
        this.clock = clock;
        this.exceptionFactory = exceptionFactory;
    }

    /**
     * Change the device's contacter state. A device command will be created to put the requested state on the
     * contactor. The comTask responsible of sending the command to the device will be triggered to run (runnow).
     * Note: loadLimit and loadTolerance are not currently supported.
     * @param mRID             The device's mrid
     * @param contactorInfo     The requested contactor state
     * @param uriInfo
     * @return HTTP 202 upon success
     * @responseheader location href to device message/command. Poll this resource to follow up on message state
     * @statuscode 404 If there is no device with the provided mRID
     * @statuscode 202 The contacter state was accepted and a message was created to send the state to the meter
     */
    @PUT
    @Transactional
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @RolesAllowed(Privileges.Constants.PUBLIC_REST_API)
    public Response updateContactor(@PathParam("mrid") String mRID, ContactorInfo contactorInfo, @Context UriInfo uriInfo) {
        Device device = deviceService.findDeviceByMrid(mRID).orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE));
        DeviceMessageId deviceMessageId = getMessageId(contactorInfo);
        ComTaskEnablement comTaskEnablement = getComTaskEnablementForDeviceMessage(device, deviceMessageId);
        Optional<ComTaskExecution> existingComTaskExecution = device.getComTaskExecutions().stream()
                .filter(cte -> cte.getComTask().getId() == comTaskEnablement.getComTask().getId())
                .findFirst();
        long messageId = createDeviceMessageOnDevice(contactorInfo, device, deviceMessageId);
        existingComTaskExecution.orElseGet(()->createAdHocComTaskExecution(device, comTaskEnablement)).runNow();

        URI uri = uriInfo.getBaseUriBuilder().
                path(DeviceMessageResource.class).
                path(DeviceMessageResource.class, "getDeviceMessage").
                build(mRID, messageId);

        LinkInfo linkInfo = new LinkInfo();
        linkInfo.link = Link.fromUri(uri).build();
        return Response.accepted(linkInfo).location(uri).build();
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
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public List<String> getFields() { // Needs to be hardcoded: no GET method => No factory to read fields from
        return Arrays.asList("status", "loadLimit", "activationDate", "loadTolerance", "callback").stream().sorted().collect(toList());
    }

    private ComTaskExecution createAdHocComTaskExecution(Device device, ComTaskEnablement comTaskEnablement) {
        ComTaskExecutionBuilder comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
        if (comTaskEnablement.hasPartialConnectionTask()) {
            device.getConnectionTasks().stream()
                    .filter(connectionTask -> connectionTask.getPartialConnectionTask().getId() == comTaskEnablement.getPartialConnectionTask().get().getId())
                    .findFirst()
                    .ifPresent(comTaskExecutionBuilder::connectionTask);
        }
        ComTaskExecution manuallyScheduledComTaskExecution = comTaskExecutionBuilder.add();
        device.save();
        return manuallyScheduledComTaskExecution;
    }

    private long createDeviceMessageOnDevice(ContactorInfo contactorInfo, Device device, DeviceMessageId deviceMessageId) {
        Device.DeviceMessageBuilder deviceMessageBuilder =
                device
                    .newDeviceMessage(deviceMessageId)
                    .setReleaseDate(clock.instant())
                ;
        if (contactorInfo.activationDate!=null) {
            deviceMessageBuilder.addProperty(DeviceMessageConstants.contactorActivationDateAttributeName,Date.from(contactorInfo.activationDate));
        }
        DeviceMessage<Device> deviceMessage = deviceMessageBuilder.add();
        return deviceMessage.getId();
    }

    private ComTaskEnablement getComTaskEnablementForDeviceMessage(Device device, DeviceMessageId deviceMessageId) {
        return device.getDeviceConfiguration().
                    getComTaskEnablements().stream().
                    filter(cte -> cte.getComTask().getProtocolTasks().stream().
                            filter(task -> task instanceof MessagesTask).
                            flatMap(task -> ((MessagesTask) task).getDeviceMessageCategories().stream()).
                            flatMap(category -> category.getMessageSpecifications().stream()).
                            filter(dms -> dms.getId().equals(deviceMessageId)).
                            findFirst().
                            isPresent()).
                    findAny().orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_COMTASK_FOR_COMMAND));
    }

    private DeviceMessageId getMessageId(ContactorInfo contactorInfo) {
        if (contactorInfo.status==null) {
            throw exceptionFactory.newException(MessageSeeds.EXPECTED_CONTACTOR_STATUS);
        }
        // TODO load limiting DeviceMessageId.LOAD_BALANCING...
        switch (contactorInfo.status) {
            case connected:
                return contactorInfo.activationDate!=null?DeviceMessageId.CONTACTOR_CLOSE_WITH_ACTIVATION_DATE:DeviceMessageId.CONTACTOR_CLOSE;
            case disconnected:
                return contactorInfo.activationDate!=null?DeviceMessageId.CONTACTOR_OPEN_WITH_ACTIVATION_DATE:DeviceMessageId.CONTACTOR_OPEN;
            case armed:
                return contactorInfo.activationDate!=null?DeviceMessageId.CONTACTOR_ARM_WITH_ACTIVATION_DATE:DeviceMessageId.CONTACTOR_ARM;
            default:
                throw exceptionFactory.newException(MessageSeeds.UNKNOWN_STATUS);
        }
    }

}
