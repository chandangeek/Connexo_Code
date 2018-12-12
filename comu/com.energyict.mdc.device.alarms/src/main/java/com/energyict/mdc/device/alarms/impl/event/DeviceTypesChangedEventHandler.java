/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.impl.event;

import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.messaging.subscriber.MessageHandler;
import com.energyict.mdc.device.alarms.impl.templates.BasicDeviceAlarmRuleTemplate;

public class DeviceTypesChangedEventHandler implements MessageHandler {
    private final BasicDeviceAlarmRuleTemplate basicAlarmDeviceTemplate;

    public DeviceTypesChangedEventHandler(BasicDeviceAlarmRuleTemplate basicDeviceAlarmRuleTemplate) {
        this.basicAlarmDeviceTemplate = basicDeviceAlarmRuleTemplate;
    }

    @Override
    public void process(Message message) {
        basicAlarmDeviceTemplate.clearAndRecalculateCache();
    }
}
