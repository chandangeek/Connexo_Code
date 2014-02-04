package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointMembership;
import com.elster.jupiter.util.time.IntermittentInterval;
import com.elster.jupiter.util.time.Interval;

public class UsagePointMembershipImpl implements UsagePointMembership {
    private final UsagePoint usagePoint;
    private IntermittentInterval intervals;

    UsagePointMembershipImpl(UsagePoint usagePoint, IntermittentInterval intervals) {
        this.usagePoint = usagePoint;
        this.intervals = intervals;
    }

    @Override
    public IntermittentInterval getIntervals() {
        return intervals;
    }

    @Override
    public UsagePoint getUsagePoint() {
        return usagePoint;
    }

    public void addInterval(Interval interval) {
        intervals = intervals.addInterval(interval);
    }

    public void removeInterval(Interval interval) {
        intervals = intervals.remove(interval);
    }

    Interval resultingInterval(Interval interval) {
        return getIntervals().intervalAt(interval.getStart());
    }

    public UsagePointMembershipImpl withIntervals(IntermittentInterval newIntervals) {
        return new UsagePointMembershipImpl(usagePoint, newIntervals);
    }
}
