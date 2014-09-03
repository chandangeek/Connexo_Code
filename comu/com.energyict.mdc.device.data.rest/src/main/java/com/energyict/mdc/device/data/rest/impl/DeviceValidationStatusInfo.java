package com.energyict.mdc.device.data.rest.impl;


import java.util.Date;

public class DeviceValidationStatusInfo {
    public boolean isActive;
    public Long lastChecked;
    public boolean hasValidation;

    public DeviceValidationStatusInfo() {
    }

    public DeviceValidationStatusInfo(boolean isActive, Date lastChecked, boolean hasValidation) {
        this.isActive = isActive;
        this.lastChecked = lastChecked == null ? null : lastChecked.getTime();
        this.hasValidation = hasValidation;
    }
}
