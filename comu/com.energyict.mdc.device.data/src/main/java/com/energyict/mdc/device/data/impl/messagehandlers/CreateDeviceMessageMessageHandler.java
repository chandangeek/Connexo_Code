/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.messagehandlers;

import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.DeviceMessageQueueMessage;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *Creates a single DeviceMessage for a single device with the provided attributes
 * Created by bvn on 3/25/15.
 */
public class CreateDeviceMessageMessageHandler implements MessageHandler {
    private static final Logger LOGGER = Logger.getLogger(CreateDeviceMessageMessageHandler.class.getSimpleName());
    private JsonService jsonService;
    private DeviceService deviceService;

    @Override
    public void process(Message message) {
        DeviceMessageQueueMessage queueMessage = jsonService.deserialize(message.getPayload(), DeviceMessageQueueMessage.class);
        Optional<Device> deviceOptional = deviceService.findDeviceById(queueMessage.deviceId);
        if (deviceOptional.isPresent()) {

        } else {
            LOGGER.log(Level.SEVERE, "Could not find device with id "+queueMessage.deviceId);
        }
    }

    @Override
    public void onMessageDelete(Message message) {

    }

    public MessageHandler init(JsonService jsonService, DeviceService deviceService) {
        this.jsonService = jsonService;
        this.deviceService = deviceService;
        return this;
    }
}
