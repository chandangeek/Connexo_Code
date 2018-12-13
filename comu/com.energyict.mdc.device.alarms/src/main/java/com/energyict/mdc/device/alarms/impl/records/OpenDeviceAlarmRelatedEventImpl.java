/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.impl.records;

import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.energyict.mdc.device.alarms.entity.DeviceAlarm;
import com.energyict.mdc.device.alarms.event.OpenDeviceAlarmRelatedEvent;

public class OpenDeviceAlarmRelatedEventImpl extends DeviceAlarmRelatedEventImpl implements OpenDeviceAlarmRelatedEvent {

    @IsPresent
    private Reference<DeviceAlarm> alarm = Reference.empty();


    DeviceAlarmRelatedEventImpl init(DeviceAlarm alarm, EndDeviceEventRecord eventRecord) {
        this.alarm.set(alarm);
        super.init(eventRecord);
        return this;
    }

}
