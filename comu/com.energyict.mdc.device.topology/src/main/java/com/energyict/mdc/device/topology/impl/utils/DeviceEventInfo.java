/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl.utils;

public class DeviceEventInfo {
    private long deviceIdentifier;

    public DeviceEventInfo(long deviceIdentifier) {
        this.setDeviceIdentifier(deviceIdentifier);
    }

    public long getDeviceIdentifier() {
        return deviceIdentifier;
    }

    public void setDeviceIdentifier(long deviceIdentifier) {
        this.deviceIdentifier = deviceIdentifier;
    }
}
