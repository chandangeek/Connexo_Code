package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.device.data.CommunicationTopologyEntry;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataService;

import com.elster.jupiter.util.time.Interval;

import java.util.Arrays;
import java.util.List;

/**
 * Provides an implementation for the {@link CommunicationTopologyEntry} interface
 * that holds a single {@link Device} and will be used to return the results
 * of querying the direct communication topology references.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-06-02 (16:09)
 * @see DeviceDataService#findCommunicationReferencingDevicesFor(Device, Interval)
 */
public final class SimpleCommunicationTopologyEntryImpl implements CommunicationTopologyEntry {

    private final Device device;
    private final Interval interval;

    public SimpleCommunicationTopologyEntryImpl(Device device, Interval interval) {
        super();
        this.device = device;
        this.interval = interval;
    }

    @Override
    public List<Device> getDevices() {
        return Arrays.asList(this.device);
    }

    @Override
    public Interval getInterval() {
        return this.interval;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof SimpleCommunicationTopologyEntryImpl) {
            return this.equals((SimpleCommunicationTopologyEntryImpl) other);
        }
        else {
            return false;
        }
    }

    public boolean equals(SimpleCommunicationTopologyEntryImpl other) {
        if (this == other) {
            return true;
        }
        else if (other == null) {
            return false;
        }
        else {
            return this.device.getId() == other.device.getId() && this.interval.equals(other.interval);
        }

    }

    @Override
    public int hashCode() {
        int result = this.device.hashCode();
        result = 31 * result + this.interval.hashCode();
        return result;
    }

}