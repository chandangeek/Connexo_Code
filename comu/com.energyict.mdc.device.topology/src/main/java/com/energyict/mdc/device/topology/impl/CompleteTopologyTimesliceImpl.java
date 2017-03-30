/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl;

import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.topology.TopologyTimeslice;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Provides an implementation for the {@link TopologyTimeslice} interface
 * that holds the complete set of {@link Device}s that are using the same
 * communication gateway in the same {@link Interval}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-06-03 (13:42)
 */
public final class CompleteTopologyTimesliceImpl implements ServerTopologyTimeslice {

    private final Range<Instant> range;
    private final List<Device> devices;
    private final Set<Long> deviceIds = new HashSet<>();

    public CompleteTopologyTimesliceImpl(Range<Instant> range, Device... devices) {
        this(range, Arrays.asList(devices));
    }

    public CompleteTopologyTimesliceImpl(Range<Instant> range, List<Device> devices) {
        super();
        this.range = range;
        this.devices = new ArrayList<>();
        this.addAll(devices);
    }

    @Override
    public CompleteTopologyTimesliceImpl asCompleteTimeslice() {
        return this;
    }

    public void add(Device... devices) {
        this.addAll(Arrays.asList(devices));
    }

    public void addAll(List<Device> devices) {
        for (Device device : devices) {
            if (!this.deviceIds.contains(device.getId())) {
                this.devices.add(device);
                this.deviceIds.add(device.getId());
            }
        }
    }

    @Override
    public List<Device> getDevices() {
        return Collections.unmodifiableList(this.devices);
    }

    @Override
    public Range<Instant> getPeriod() {
        return this.range;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof CompleteTopologyTimesliceImpl) {
            return this.doEquals((CompleteTopologyTimesliceImpl) other);
        } else {
            return false;
        }
    }

    private boolean doEquals(CompleteTopologyTimesliceImpl other) {
        if (this == other) {
            return true;
        } else if (other == null) {
            return false;
        } else {
            return this.range.equals(other.range);
        }

    }

    @Override
    public int hashCode() {
        return this.range.hashCode();
    }

}