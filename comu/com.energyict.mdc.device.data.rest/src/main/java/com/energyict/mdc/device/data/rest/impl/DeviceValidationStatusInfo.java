package com.energyict.mdc.device.data.rest.impl;

import java.time.Instant;
import java.util.Optional;

public class DeviceValidationStatusInfo extends ValidationStatusInfo {
    public Long registerSuspectCount;
    public Long loadProfileSuspectCount;
    public DeviceInfo device;

    public DeviceValidationStatusInfo() {
    }

    public DeviceValidationStatusInfo(boolean isActive, boolean isOnStorage, Optional<Instant> lastChecked, boolean hasValidation) {
        super(isActive, isOnStorage, lastChecked, hasValidation);
    }

}