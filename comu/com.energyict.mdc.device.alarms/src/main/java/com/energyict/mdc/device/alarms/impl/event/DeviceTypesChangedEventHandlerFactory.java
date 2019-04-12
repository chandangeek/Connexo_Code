/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.impl.event;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.elster.jupiter.messaging.subscriber.MessageHandlerFactory;
import com.energyict.mdc.device.alarms.impl.ModuleConstants;
import com.energyict.mdc.device.alarms.impl.templates.BasicDeviceAlarmRuleTemplate;

import com.energyict.mdc.device.config.DeviceConfigurationService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

@Component(name = "com.energyict.mdc.device.alarms.DeviceTypesChangedEventHandlerFactory",
        service = MessageHandlerFactory.class,
        property = {"subscriber=" + ModuleConstants.DEVICE_TYPES_CHANGES_SUBSC, "destination=" + EventService.JUPITER_EVENTS},
        immediate = true)
public class DeviceTypesChangedEventHandlerFactory implements MessageHandlerFactory {
    private volatile DeviceConfigurationService deviceConfigurationService;

    public DeviceTypesChangedEventHandlerFactory() {
        //for osgi
    }

    @Inject
    public DeviceTypesChangedEventHandlerFactory(DeviceConfigurationService deviceConfigurationService) {
        setDeviceConfigurationService(deviceConfigurationService);
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Override
    public MessageHandler newMessageHandler() {
        return new DeviceTypesChangedEventHandler(deviceConfigurationService);
    }
}
