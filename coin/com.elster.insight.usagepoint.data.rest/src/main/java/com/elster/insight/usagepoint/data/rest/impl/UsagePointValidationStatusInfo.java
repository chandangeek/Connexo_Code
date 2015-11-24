package com.elster.insight.usagepoint.data.rest.impl;

import java.time.Instant;
import java.util.Optional;

public class UsagePointValidationStatusInfo extends ValidationStatusInfo {
    public Long registerSuspectCount;
    public Long channelSuspectCount;
    public UsagePointInfo usagePoint;

    public UsagePointValidationStatusInfo() {
    }

    public UsagePointValidationStatusInfo(boolean isActive, boolean isOnStorage, Optional<Instant> lastChecked, boolean hasValidation) {
        super(isActive, isOnStorage, lastChecked, hasValidation);
    }

}