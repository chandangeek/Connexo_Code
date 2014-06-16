package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.device.data.Device;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Provides an implementation for the {@link CommunicationTopology} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-06-03 (10:29)
 */
public final class CommunicationTopologyImpl implements CommunicationTopology {

    private final Device root;
    private final Interval interval;
    private final List<CommunicationTopology> children;

    public CommunicationTopologyImpl(Device root, Interval interval) {
        super();
        this.root = root;
        this.interval = interval;
        this.children = new ArrayList<>();
    }

    @Override
    public Device getRoot() {
        return this.root;
    }

    @Override
    public Interval getInterval() {
        return this.interval;
    }

    @Override
    public List<Device> getDevices() {
        List<Device> devices = new ArrayList<>();
        for (CommunicationTopology child : this.children) {
            devices.add(child.getRoot());
        }
        return devices;
    }

    @Override
    public boolean isLeaf() {
        return this.children.isEmpty();
    }

    @Override
    public List<CommunicationTopology> getChildren() {
        return Collections.unmodifiableList(this.children);
    }

    @Override
    public boolean addChild(CommunicationTopology newChild) {
        this.children.add(newChild);
        return true;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof CommunicationTopologyImpl) {
            return this.equals((CommunicationTopologyImpl) other);
        }
        else {
            return false;
        }
    }

    public boolean equals(CommunicationTopologyImpl other) {
        if (this == other) {
            return true;
        }
        else if (other == null) {
            return false;
        }
        else {
            return this.root.getId() == other.root.getId() && this.interval.equals(other.interval);
        }

    }

    @Override
    public int hashCode() {
        int result = this.root.hashCode();
        result = 31 * result + this.interval.hashCode();
        return result;
    }

}