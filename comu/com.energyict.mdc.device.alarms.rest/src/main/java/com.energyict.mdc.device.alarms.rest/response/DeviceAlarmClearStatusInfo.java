/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.rest.response;

import com.energyict.mdc.device.alarms.entity.DeviceAlarmClearStatus;

import java.time.Instant;

public class DeviceAlarmClearStatusInfo {

    public boolean statusValue;
    public Instant statusChangeDateTime;


    public DeviceAlarmClearStatusInfo() {

    }

    public DeviceAlarmClearStatusInfo(DeviceAlarmClearStatus deviceAlarmClearStatus) {
        if (deviceAlarmClearStatus != null) {
            statusValue = deviceAlarmClearStatus.isCleared();
            statusChangeDateTime = deviceAlarmClearStatus.getStatusChangeTime();
        }
    }

}
