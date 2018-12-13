/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl;

import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.topology.TopologyTimeslice;

import com.google.common.collect.Range;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

/**
 * Provides an implementation for the {@link TopologyTimeslice} interface
 * that holds a single {@link Device} and will be used to return the results
 * of querying the direct topology references.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @see DeviceService#buildCommunicationTopology(Device, Range)
 * @see DeviceService#buildPhysicalTopology(Device, Range)
 * @since 2014-06-02 (16:09)
 */
public final class SimpleTopologyTimesliceImpl implements ServerTopologyTimeslice {

    private final Device device;
    private final Range<Instant> period;

    public SimpleTopologyTimesliceImpl(Device device, Interval interval) {
        super();
        this.device = device;
        this.period = this.roundToSeconds(interval).toClosedRange();
    }

    private Interval roundToSeconds(Interval interval) {
        if (interval.getStart() == null) {
            if (interval.getEnd() == null) {
                return interval;
            }
            else {
                return Interval.endAt(interval.getEnd().truncatedTo(ChronoUnit.SECONDS));
            }
        }
        else {
            if (interval.getEnd() == null) {
                return Interval.startAt(interval.getStart().truncatedTo(ChronoUnit.SECONDS));
            }
            else {
                return Interval.of(
                        interval.getStart().truncatedTo(ChronoUnit.SECONDS),
                        interval.getEnd().truncatedTo(ChronoUnit.SECONDS));
            }
        }
    }

    @Override
    public CompleteTopologyTimesliceImpl asCompleteTimeslice() {
        return new CompleteTopologyTimesliceImpl(this.period, this.getDevices());
    }

    @Override
    public List<Device> getDevices() {
        return Arrays.asList(this.device);
    }

    @Override
    public Range<Instant> getPeriod() {
        return this.period;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof SimpleTopologyTimesliceImpl) {
            return this.doEquals((SimpleTopologyTimesliceImpl) other);
        } else {
            return false;
        }
    }

    private boolean doEquals(SimpleTopologyTimesliceImpl other) {
        if (this == other) {
            return true;
        } else if (other == null) {
            return false;
        } else {
            return this.device.getId() == other.device.getId() && this.period.equals(other.period);
        }

    }

    @Override
    public int hashCode() {
        int result = this.device.hashCode();
        result = 31 * result + this.period.hashCode();
        return result;
    }

}