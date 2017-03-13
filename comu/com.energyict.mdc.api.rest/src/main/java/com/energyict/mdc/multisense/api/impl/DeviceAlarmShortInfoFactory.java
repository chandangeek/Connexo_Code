/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.impl;

import com.energyict.mdc.device.alarms.entity.DeviceAlarm;

import javax.inject.Inject;

public class DeviceAlarmShortInfoFactory {

    private final DeviceAlarmStatusInfoFactory deviceAlarmStatusInfoFactory;

    @Inject
    public DeviceAlarmShortInfoFactory(DeviceAlarmStatusInfoFactory deviceAlarmStatusInfoFactory) {
        this.deviceAlarmStatusInfoFactory = deviceAlarmStatusInfoFactory;
    }

    public DeviceAlarmShortInfo asInfo(DeviceAlarm alarm) {
        DeviceAlarmShortInfo info = new DeviceAlarmShortInfo();
        info.id = alarm.getId();
        info.title = alarm.getTitle();
        info.status = deviceAlarmStatusInfoFactory.from(alarm.getStatus(), alarm.isStatusCleared(), null, null);
        info.version = alarm.getVersion();
        return info;
    }
}
