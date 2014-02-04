package com.elster.jupiter.metering.groups;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.util.time.IntermittentInterval;

public interface UsagePointMembership {

    IntermittentInterval getIntervals();

    UsagePoint getUsagePoint();
}
