/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import java.time.Instant;

public class ValidationStatusInfo {
    public boolean isActive;
    public boolean isStorage;
    public Instant lastChecked;
    public Instant lastRun;

    public boolean hasValidation;
    public Boolean allDataValidated = true;
    public Boolean validateOnStorageConfiguration;

    public ValidationStatusInfo() {
    }

    public ValidationStatusInfo(boolean isActive, Instant lastChecked, boolean hasValidation) {
        this.isActive = isActive;
        this.lastChecked = lastChecked;
        this.hasValidation = hasValidation;
    }

    public ValidationStatusInfo(boolean isActive, boolean isStorage, Instant lastChecked, boolean hasValidation) {
        this(isActive, lastChecked, hasValidation);
        this.isStorage = isStorage;
    }

    public ValidationStatusInfo(boolean isActive, boolean isStorage, Instant lastChecked, Instant lastRun,
                                boolean hasValidation, boolean validateOnStorageConfiguration) {
        this(isActive, isStorage, lastChecked, hasValidation);
        this.lastRun = lastRun;
        this.validateOnStorageConfiguration = validateOnStorageConfiguration;
    }
}
