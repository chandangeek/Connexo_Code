/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl.utils;

public class DeviceEventInfo {
    private long deviceIdentifier;
    private long gatewayIdentifier;

    public DeviceEventInfo(long deviceIdentifier) {
        this.setDeviceIdentifier(deviceIdentifier);
    }

    public DeviceEventInfo(long deviceIdentifier, long gatewayIdentifier) {
        this.setDeviceIdentifier(deviceIdentifier);
        this.setGatewayIdentifier(gatewayIdentifier);
    }

    public long getDeviceIdentifier() {
        return deviceIdentifier;
    }

    public void setDeviceIdentifier(long deviceIdentifier) {
        this.deviceIdentifier = deviceIdentifier;
    }

    public long getGatewayIdentifier() {
        return gatewayIdentifier;
    }

    public void setGatewayIdentifier(long gatewayIdentifier) {
        this.gatewayIdentifier = gatewayIdentifier;
    }
}
