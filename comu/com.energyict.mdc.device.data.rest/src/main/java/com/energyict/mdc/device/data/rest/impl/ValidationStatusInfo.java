package com.energyict.mdc.device.data.rest.impl;

import java.time.Instant;
import java.util.Optional;

public class ValidationStatusInfo {
    public boolean isActive;
    public boolean isStorage;
    public Long lastChecked;
    public boolean hasValidation;
    public Boolean allDataValidated = true;

    public ValidationStatusInfo() {
    }

    public ValidationStatusInfo(boolean isActive, Optional<Instant> lastChecked, boolean hasValidation) {
        this.isActive = isActive;
        if (lastChecked.isPresent()) {
            this.lastChecked = lastChecked.get().toEpochMilli();
        }else{
            this.lastChecked = null;
        }
        this.hasValidation = hasValidation;
    }

    public ValidationStatusInfo(boolean isActive, boolean isStorage, Optional<Instant> lastChecked, boolean hasValidation) {
        this(isActive, lastChecked, hasValidation);
        this.isStorage = isStorage;
    }
}
