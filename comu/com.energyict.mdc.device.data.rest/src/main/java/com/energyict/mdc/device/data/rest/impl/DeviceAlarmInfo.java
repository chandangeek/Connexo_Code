/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;
import com.energyict.mdc.device.alarms.entity.DeviceAlarm;
import com.energyict.mdc.device.alarms.entity.DeviceAlarmClearStatus;
import com.elster.jupiter.issue.rest.response.device.DeviceInfo;
import com.elster.jupiter.issue.rest.response.issue.IssueInfo;

import java.time.Instant;

public class DeviceAlarmInfo<T extends DeviceInfo> extends IssueInfo<T, DeviceAlarm> {
    public DeviceAlarmClearStatusInfo clearedStatus;


    DeviceAlarmInfo(DeviceAlarm alarm, Class<T> deviceInfoClass){
        super(alarm, deviceInfoClass);
        clearedStatus = new DeviceAlarmClearStatusInfo(alarm.getClearStatus());
    }

    private class DeviceAlarmClearStatusInfo {

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
}