package com.energyict.mdc.device.data.rest.impl;

import java.util.Date;

public class DeviceValidationStatusInfo extends ValidationStatusInfo {
    public Long registerSuspectCount;
    public Long loadProfileSuspectCount;

    public DeviceValidationStatusInfo() {
    }

    public DeviceValidationStatusInfo(boolean isActive, Date lastChecked, boolean hasValidation) {
        super(isActive, lastChecked, hasValidation);
    }

}