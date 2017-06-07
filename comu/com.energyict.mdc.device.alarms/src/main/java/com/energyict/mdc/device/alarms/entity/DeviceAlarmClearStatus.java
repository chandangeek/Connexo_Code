/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.entity;


import java.time.Instant;

public final class DeviceAlarmClearStatus {

    boolean statusValue;
    Instant statusChangeDateTime;

    public DeviceAlarmClearStatus() {
    }

    public boolean isCleared() {
        return statusValue;
    }

    public Instant getStatusChangeTime() {
        return statusChangeDateTime;
    }

    public void toggle(Instant timeStamp) {
        statusValue = !statusValue;
        statusChangeDateTime = timeStamp;
    }

    public void init() {
        statusValue = Boolean.FALSE;
        statusChangeDateTime = Instant.EPOCH;
    }
}
