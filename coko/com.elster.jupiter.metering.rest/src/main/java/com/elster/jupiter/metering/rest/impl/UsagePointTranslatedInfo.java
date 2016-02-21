package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.rest.UsagePointInfo;

import java.time.Clock;

public class UsagePointTranslatedInfo extends UsagePointInfo {
    public String displayServiceCategory;
    public String displayConnectionState;
    public String displayAmiBillingReady;

    public UsagePointTranslatedInfo(UsagePoint usagePoint, Clock clock) {
        super(usagePoint, clock);

    }
}