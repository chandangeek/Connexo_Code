package com.energyict.mdc.device.data.rest.impl;


import java.util.Date;

public class DeviceValidationStatusInfo {
    public boolean isActive;
    public Date lastChecked;

    public DeviceValidationStatusInfo() {}

    public DeviceValidationStatusInfo(boolean isActive, Date lastChecked) {
        this.isActive = isActive;
        this.lastChecked = lastChecked;
    }
}
