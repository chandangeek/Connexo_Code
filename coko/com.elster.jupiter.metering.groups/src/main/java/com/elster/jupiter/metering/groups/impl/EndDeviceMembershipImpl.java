package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.groups.EndDeviceMembership;
import com.elster.jupiter.util.time.IntermittentInterval;
import com.elster.jupiter.util.time.Interval;

public class EndDeviceMembershipImpl implements EndDeviceMembership {
    private final EndDevice endDevice;
    private IntermittentInterval intervals;

    EndDeviceMembershipImpl(EndDevice endDevice, IntermittentInterval intervals) {
        this.endDevice = endDevice;
        this.intervals = intervals;
    }

    @Override
    public IntermittentInterval getIntervals() {
        return intervals;
    }

    @Override
    public EndDevice getEndDevice() {
        return endDevice;
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

    public EndDeviceMembershipImpl withIntervals(IntermittentInterval newIntervals) {
        return new EndDeviceMembershipImpl(endDevice, newIntervals);
    }
}
