package com.elster.jupiter.kore.api.impl;

import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointDetailBuilder;

import java.time.Instant;

/**
 * Created by bvn on 4/11/16.
 */
public class DefaultUsagePointInfo extends UsagePointInfo {

    @Override
    public UsagePointDetailBuilder createDetail(UsagePoint usagePoint, Instant instant) {
        return null;
    }

    @Override
    ServiceKind getServiceKind() {
        return null;
    }
}
