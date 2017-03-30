/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import java.time.Instant;

public class DeviceValidationStatusInfo extends ValidationStatusInfo {
    public Long registerSuspectCount;
    public Long loadProfileSuspectCount;
    public DeviceInfo device;

    public DeviceValidationStatusInfo() {
    }

    public DeviceValidationStatusInfo(boolean isActive, boolean isOnStorage, Instant lastChecked, Instant lastRun,
                                      boolean hasValidation, boolean validateOnStorageConfiguration) {
        super(isActive, isOnStorage, lastChecked, lastRun, hasValidation, validateOnStorageConfiguration);
    }

}
