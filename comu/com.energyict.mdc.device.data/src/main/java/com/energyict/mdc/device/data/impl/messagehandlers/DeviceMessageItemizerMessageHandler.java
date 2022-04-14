/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.messagehandlers;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.device.data.QueueMessage;
import com.energyict.mdc.device.data.tasks.BulkDeviceMessageQueueMessage;
import com.energyict.mdc.device.data.tasks.DeviceMessageQueueMessage;

import java.time.Clock;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Will 'itemize' the creation of a single device message with the provided attributes on all devices in a device group.
 *
 * Created by bvn on 3/25/15.
 */
public class DeviceMessageItemizerMessageHandler implements MessageHandler {
    private static final Logger LOGGER = Logger.getLogger(DeviceMessageItemizerMessageHandler.class.getSimpleName());
    private JsonService jsonService;
    private MeteringGroupsService meteringGroupsService;
    private Clock clock;
    private MessageService messageService;

    @Override
    public void process(Message message) {
        Optional<DestinationSpec> destinationSpec = messageService.getDestinationSpec(DeviceMessageService.DEVICE_MESSAGE_QUEUE_DESTINATION);
        if (destinationSpec.isPresent()) {
            BulkDeviceMessageQueueMessage queueMessage = jsonService.deserialize(message.getPayload(), BulkDeviceMessageQueueMessage.class);
            Optional<EndDeviceGroup> deviceGroupOptional = meteringGroupsService.findEndDeviceGroup(queueMessage.endDeviceGroupId);
            if (deviceGroupOptional.isPresent()) {
                for (EndDevice endDevice : deviceGroupOptional.get().getMembers(clock.instant())) {
                    try {
                        long deviceId = Long.parseLong(endDevice.getAmrId());
                        DeviceMessageQueueMessage deviceMessage = new DeviceMessageQueueMessage(deviceId, queueMessage.deviceMessageId, queueMessage.releaseDate, queueMessage.properties, queueMessage.createdByUser, queueMessage.trigger);
                        processMessagePost(deviceMessage, destinationSpec.get());
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, String.format("Failed to create message for device with amr id '%s' : %s", endDevice.getAmrId(), e.getLocalizedMessage()));
                    }
                }
            } else {
                LOGGER.log(Level.SEVERE, "Could not locate EndDeviceGroup with id "+queueMessage.deviceMessageId);
            }
        } else {
            LOGGER.log(Level.SEVERE, String.format("The message destination spec '%s' could not be found", DeviceMessageService.DEVICE_MESSAGE_QUEUE_DESTINATION));
        }
    }

    private void processMessagePost(QueueMessage message, DestinationSpec destinationSpec) {
        String json = jsonService.serialize(message);
        destinationSpec.message(json).send();
    }

    @Override
    public void onMessageDelete(Message message) {

    }

    public MessageHandler init(JsonService jsonService, MeteringGroupsService meteringGroupsService, Clock clock, MessageService messageService) {
        this.jsonService = jsonService;
        this.meteringGroupsService = meteringGroupsService;
        this.clock = clock;
        this.messageService = messageService;
        return this;
    }
}
