/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.impl;

import com.energyict.mdc.device.alarms.entity.DeviceAlarm;

import javax.inject.Inject;

public class AlarmShortInfoFactory {

    private final AlarmStatusInfoFactory alarmStatusInfoFactory;

    @Inject
    public AlarmShortInfoFactory(AlarmStatusInfoFactory alarmStatusInfoFactory) {
        this.alarmStatusInfoFactory = alarmStatusInfoFactory;
    }

    public AlarmShortInfo asInfo(DeviceAlarm alarm) {
        AlarmShortInfo info = new AlarmShortInfo();
        info.id = alarm.getId();
        info.title = alarm.getTitle();
        info.status = alarmStatusInfoFactory.from(alarm.getStatus(), alarm.isStatusCleared(), null, null);
        info.version = alarm.getVersion();
        return info;
    }
}
