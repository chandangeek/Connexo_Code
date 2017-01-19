package com.energyict.mdc.upl.meterdata;

import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;

import java.time.Duration;

public class TopologyPathSegment {

    private final DeviceIdentifier source;
    private final DeviceIdentifier target;
    private final DeviceIdentifier intermediateHop;
    private final Duration timeToLive;
    private final int cost;

    public TopologyPathSegment(DeviceIdentifier source, DeviceIdentifier target, DeviceIdentifier intermediateHop, Duration timeToLive, int cost) {
        this.source = source;
        this.target = target;
        this.intermediateHop = intermediateHop;
        this.timeToLive = timeToLive;
        this.cost = cost;
    }

    public DeviceIdentifier getSource() {
        return source;
    }

    public DeviceIdentifier getTarget() {
        return target;
    }

    public DeviceIdentifier getIntermediateHop() {
        return intermediateHop;
    }

    public Duration getTimeToLive() {
        return timeToLive;
    }

    public int getCost() {
        return cost;
    }
}