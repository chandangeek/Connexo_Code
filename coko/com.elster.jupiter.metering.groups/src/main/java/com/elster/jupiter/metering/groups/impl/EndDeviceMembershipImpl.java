package com.elster.jupiter.metering.groups.impl;

import java.time.Instant;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.groups.EndDeviceMembership;
import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

public class EndDeviceMembershipImpl implements EndDeviceMembership {
    private final EndDevice endDevice;
    private RangeSet<Instant> ranges;

    EndDeviceMembershipImpl(EndDevice endDevice, RangeSet<Instant> ranges) {
        this.endDevice = endDevice;
        this.ranges = TreeRangeSet.create(ranges);
    }

    @Override
    public RangeSet<Instant> getRanges() {
        return ImmutableRangeSet.copyOf(ranges);
    }

    @Override
    public EndDevice getEndDevice() {
        return endDevice;
    }

    public void addRange(Range<Instant> range) {
        ranges.add(range);
    }

    public void removeRange(Range<Instant> range) {
        ranges.remove(range);
    }

    Range<Instant> resultingRange(Range<Instant> range) { 
        return getRanges().rangeContaining(range.hasLowerBound() ? range.lowerEndpoint() : Instant.MIN);
    }

    public EndDeviceMembershipImpl withRanges(RangeSet<Instant> newRanges) {
        return new EndDeviceMembershipImpl(endDevice, newRanges);
    }
}
