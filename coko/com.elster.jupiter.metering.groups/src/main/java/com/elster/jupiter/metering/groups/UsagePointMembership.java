package com.elster.jupiter.metering.groups;

import java.time.Instant;

import com.elster.jupiter.metering.UsagePoint;
import com.google.common.collect.RangeSet;

public interface UsagePointMembership {

    RangeSet<Instant> getRanges();

    UsagePoint getUsagePoint();
}
