/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.config.DeviceConfigurationService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

@Component(name = "com.energyict.mdc.device.config.DeviceTypesChangedEventHandlerFactory",
        service = MessageHandlerFactory.class,
        property = {"subscriber=" + "DeviceTypesChanges", "destination=" + EventService.JUPITER_EVENTS},
        immediate = true)
public class DeviceTypesChangedEventHandlerFactory implements MessageHandlerFactory {
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile EventService eventService;
    private volatile JsonService jsonService;

    public DeviceTypesChangedEventHandlerFactory() {
        //for osgi
    }

    @Inject
    public DeviceTypesChangedEventHandlerFactory(DeviceConfigurationService deviceConfigurationService,
                                                 EventService eventService,
                                                 JsonService jsonService) {
        setDeviceConfigurationService(deviceConfigurationService);
        setEventService(eventService);
        setJsonService(jsonService);
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    @Reference
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    @Override
    public MessageHandler newMessageHandler() {
        return new DeviceTypeChangedEventHandler((DeviceConfigurationServiceImpl) deviceConfigurationService, eventService, jsonService);
    }
}
