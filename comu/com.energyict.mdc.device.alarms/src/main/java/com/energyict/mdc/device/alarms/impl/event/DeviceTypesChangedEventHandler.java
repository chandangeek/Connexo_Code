/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.impl.event;

import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.energyict.mdc.device.alarms.impl.templates.BasicDeviceAlarmRuleTemplate;
import com.energyict.mdc.device.config.DeviceConfigurationService;

public class DeviceTypesChangedEventHandler implements MessageHandler {
    private final DeviceConfigurationService deviceConfigurationService;

    public DeviceTypesChangedEventHandler(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Override
    public void process(Message message) {
        deviceConfigurationService.clearAndRecalculateCache();
    }
}
