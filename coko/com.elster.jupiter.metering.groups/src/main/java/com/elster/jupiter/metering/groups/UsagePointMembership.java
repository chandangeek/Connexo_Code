package com.elster.jupiter.metering.groups;

import com.elster.jupiter.metering.UsagePoint;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.RangeSet;

import java.time.Instant;

@ProviderType
public interface UsagePointMembership {

    RangeSet<Instant> getRanges();

    UsagePoint getUsagePoint();

}