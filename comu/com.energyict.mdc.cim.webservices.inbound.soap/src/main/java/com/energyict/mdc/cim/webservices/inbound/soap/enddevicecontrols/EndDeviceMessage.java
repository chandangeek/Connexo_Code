/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.enddevicecontrols;

public class EndDeviceMessage {
    private String deviceName;
    private String deviceMrid;

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceMrid() {
        return deviceMrid;
    }

    public void setDeviceMrid(String deviceMrid) {
        this.deviceMrid = deviceMrid;
    }
}
