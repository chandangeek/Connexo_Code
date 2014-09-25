package com.energyict.mdc.device.data.rest.impl;

import java.util.Date;

public class ValidationStatusInfo {
    public boolean isActive;
    public Long lastChecked;
    public boolean hasValidation;
    public Boolean allDataValidated = false;

    public ValidationStatusInfo() {
    }

    public ValidationStatusInfo(boolean isActive, Date lastChecked, boolean hasValidation) {
        this.isActive = isActive;
        this.lastChecked = lastChecked == null ? null : lastChecked.getTime();
        this.hasValidation = hasValidation;
    }

}
