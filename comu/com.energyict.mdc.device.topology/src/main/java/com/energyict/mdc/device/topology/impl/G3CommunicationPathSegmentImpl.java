/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl;

import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.topology.G3CommunicationPathSegment;

import java.time.Duration;
import java.util.Optional;

/**
 * Provides an implementation for the {@link G3CommunicationPathSegment} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-08 (14:42)
 */
public class G3CommunicationPathSegmentImpl extends CommunicationPathSegmentImpl implements G3CommunicationPathSegment {

    private Reference<Device> nextHop = ValueReference.absent();
    private long timeToLive;
    private int cost;

    G3CommunicationPathSegmentImpl createIntermediate(Device source, Device target, Interval interval, Device nextHop, long timeToLiveSeconds, int cost) {
        super.init(source, target, interval);
        this.nextHop.set(nextHop);
        this.setTimeToLiveFromSeconds(timeToLiveSeconds);
        this.setCost(cost);
        return this;
    }

    G3CommunicationPathSegmentImpl createFinal(Device source, Device target, Interval interval, long timeToLiveSeconds, int cost) {
        super.init(source, target, interval);
        this.setTimeToLiveFromSeconds(timeToLiveSeconds);
        this.setCost(cost);
        return this;
    }

    @Override
    public Optional<Device> getNextHopDevice() {
        return nextHop.getOptional();
    }

    @Override
    public Duration getTimeToLive() {
        return Duration.ofSeconds(this.timeToLive);
    }

    void setTimeToLiveFromSeconds(long seconds) {
        this.timeToLive = seconds;
    }

    @Override
    public int getCost() {
        return this.cost;
    }

    void setCost(int cost) {
        this.cost = cost;
    }

}