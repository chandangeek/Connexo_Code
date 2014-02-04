package com.elster.jupiter.metering;

import com.elster.jupiter.util.time.IntermittentInterval;

public interface UsagePointMembership {

    IntermittentInterval getIntervals();

    UsagePoint getUsagePoint();
}
