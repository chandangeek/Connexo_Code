/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.common.device.config.EventType;

import java.util.Map;
import java.util.logging.Logger;

public class DeviceTypeChangedEventHandler implements MessageHandler {
    private final DeviceConfigurationServiceImpl deviceConfigurationService;
    private final EventService eventService;
    private final JsonService jsonService;
    private final static Logger LOGGER = Logger.getLogger(DeviceTypeChangedEventHandler.class.getName());

    public DeviceTypeChangedEventHandler(DeviceConfigurationServiceImpl deviceConfigurationService, EventService eventService, JsonService jsonService) {
        this.deviceConfigurationService = deviceConfigurationService;
        this.eventService = eventService;
        this.jsonService = jsonService;
    }

    @Override
    public void process(Message message) {
        deviceConfigurationService.clearAndRecalculateCache();
        Map<String, Object> messageProperties = this.jsonService.deserialize(message.getPayload(), Map.class);
        if (messageProperties.get("id") != null) {
            long deviceTypeId = ((Number) messageProperties.get("id")).longValue();
            if (!deviceConfigurationService.findDeviceType(deviceTypeId).isPresent()) {
                LOGGER.warning("Device type with id " + deviceTypeId + " no longer exists");
            } else {
                eventService.postEvent(EventType.DEVICE_TYPE_LIFE_CYCLE_CACHE_RECALCULATED.topic(), deviceConfigurationService.findDeviceType(deviceTypeId).get());
            }
        }
    }
}