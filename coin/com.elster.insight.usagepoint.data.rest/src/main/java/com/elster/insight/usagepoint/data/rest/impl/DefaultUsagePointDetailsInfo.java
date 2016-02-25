package com.elster.insight.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointDetail;
import com.elster.jupiter.metering.UsagePointDetailBuilder;

import java.time.Clock;

public class DefaultUsagePointDetailsInfo extends BaseUsagePointDetailsInfo {

    public DefaultUsagePointDetailsInfo() {
    }

    public DefaultUsagePointDetailsInfo(UsagePointDetail detail) {
        super(detail);
    }

    @Override
    public UsagePointDetailBuilder getUsagePointDetailBuilder(UsagePoint usagePoint, Clock clock) {
        return usagePoint.newDefaultDetailBuilder(clock.instant());
    }
}
