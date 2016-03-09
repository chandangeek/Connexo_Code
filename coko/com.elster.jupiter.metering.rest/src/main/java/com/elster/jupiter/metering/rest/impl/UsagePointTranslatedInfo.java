package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.metering.UsagePoint;

import java.time.Clock;

public class UsagePointTranslatedInfo extends UsagePointInfo {
    public String displayServiceCategory;

    public UsagePointTranslatedInfo(UsagePoint usagePoint, Clock clock) {
        super(usagePoint, clock);

    }
}