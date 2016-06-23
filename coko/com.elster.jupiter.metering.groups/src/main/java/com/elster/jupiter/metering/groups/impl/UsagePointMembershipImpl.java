package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.groups.UsagePointMembership;

import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

import java.time.Instant;

class UsagePointMembershipImpl implements UsagePointMembership {
    private final UsagePoint usagePoint;
    private RangeSet<Instant> ranges;

    UsagePointMembershipImpl(UsagePoint usagePoint, RangeSet<Instant> ranges) {
        this.usagePoint = usagePoint;
        this.ranges = TreeRangeSet.create(ranges);
    }

    @Override
    public RangeSet<Instant> getRanges() {
        return ImmutableRangeSet.copyOf(ranges);
    }

    @Override
    public UsagePoint getUsagePoint() {
        return usagePoint;
    }

    void addRange(Range<Instant> range) {
        ranges.add(range);
    }

    void removeRange(Range<Instant> range) {
        ranges.remove(range);
    }

    Range<Instant> resultingRange(Range<Instant> range) {
        return ranges.rangeContaining(range.hasLowerBound() ? range.lowerEndpoint() : Instant.MIN);
    }

    UsagePointMembershipImpl withRanges(RangeSet<Instant> newRanges) {
        return new UsagePointMembershipImpl(usagePoint, newRanges);
    }

}