package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.device.data.CommunicationTopologyEntry;
import com.energyict.mdc.device.data.Device;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Provides an implementation for the {@link CommunicationTopologyEntry} interface
 * that holds the complete set of {@link Device}s that are using the same
 * communication gateway in the same {@link Interval}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-06-03 (13:42)
 */
public final class CompleteCommunicationTopologyEntryImpl implements CommunicationTopologyEntry {

    private final Interval interval;
    private final List<Device> devices;
    private final Set<Long> deviceIds = new HashSet<>();

    public CompleteCommunicationTopologyEntryImpl(Interval interval, Device... devices) {
        this(interval, Arrays.asList(devices));
    }

    public CompleteCommunicationTopologyEntryImpl(Interval interval, List<Device> devices) {
        super();
        this.interval = interval;
        this.devices = new ArrayList<>();
        this.addAll(devices);
    }

    public void add (Device... devices) {
        this.addAll(Arrays.asList(devices));
    }

    public void addAll (List<Device> devices) {
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
    public Interval getInterval() {
        return this.interval;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof CompleteCommunicationTopologyEntryImpl) {
            return this.equals((CompleteCommunicationTopologyEntryImpl) other);
        }
        else {
            return false;
        }
    }

    public boolean equals(CompleteCommunicationTopologyEntryImpl other) {
        if (this == other) {
            return true;
        }
        else if (other == null) {
            return false;
        }
        else {
            return this.interval.equals(other.interval);
        }

    }

    @Override
    public int hashCode() {
        return this.interval.hashCode();
    }

}