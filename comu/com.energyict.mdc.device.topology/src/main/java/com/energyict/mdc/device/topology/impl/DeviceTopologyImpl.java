/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.topology.DeviceTopology;
import com.energyict.mdc.device.topology.TopologyTimeline;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Provides an implementation for the {@link DeviceTopology} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-06-03 (10:29)
 */
public final class DeviceTopologyImpl implements DeviceTopology {

    private final Device root;
    private final Range<Instant> period;
    private final List<DeviceTopologyImpl> children;

    public DeviceTopologyImpl(Device root, Range<Instant> period) {
        super();
        this.root = root;
        this.period = period;
        this.children = new ArrayList<>();
    }

    @Override
    public Device getRoot() {
        return this.root;
    }

    @Override
    public Range<Instant> getPeriod() {
        return this.period;
    }

    @Override
    public List<Device> getDevices() {
        return this.children
                .stream()
                .map(DeviceTopology::getRoot)
                .collect(Collectors.toList());
    }

    @Override
    public Set<Device> getAllDevices() {
        List<Device> allDevices = new ArrayList<>();
        this.addAllDevices(allDevices);
        return this.toSet(allDevices);
    }

    private void addAllDevices(List<Device> allDevices) {
        allDevices.addAll(this.getDevices());
        this.children
                .stream()
                .forEach(c -> c.addAllDevices(allDevices));
    }

    private Set<Device> toSet(List<Device> devices) {
        Map<Long, Device> devicesById = new HashMap<>();
        devices.stream().forEach(d -> devicesById.put(d.getId(), d));
        return new HashSet<>(devicesById.values());
    }

    @Override
    public TopologyTimeline timelined() {
        TopologyTimesliceMerger merger = new TopologyTimesliceMerger();
        this.buildTimeline(merger, Collections.emptyList());
        return TopologyTimelineImpl.forAll(merger.getEntries());
    }

    private void buildTimeline(TopologyTimesliceMerger merger, List<Device> intermediateGateways) {
        this.children
                .stream()
                .filter(DeviceTopologyImpl::isLeaf)
                .forEach(c -> merger.add(new CompleteTopologyTimesliceImpl(c.getPeriod(), this.append(intermediateGateways, c.getRoot()))));
        this.children
                .stream()
                .filter(c -> !c.isLeaf())
                .forEach(c -> c.buildTimeline(merger, this.append(intermediateGateways, c.getRoot())));
    }

    private List<Device> append(List<Device> devices, Device oneMoreDevice) {
        List<Device> concatenated = new ArrayList<>(devices);
        concatenated.add(oneMoreDevice);
        return concatenated;
    }

    @Override
    public boolean isLeaf() {
        return this.children.isEmpty();
    }

    @Override
    public List<DeviceTopology> getChildren() {
        return Collections.unmodifiableList(this.children);
    }

    public boolean addChild(DeviceTopologyImpl newChild) {
        this.children.add(newChild);
        return true;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof DeviceTopologyImpl) {
            return this.doEquals((DeviceTopologyImpl) other);
        } else {
            return false;
        }
    }

    private boolean doEquals(DeviceTopologyImpl other) {
        if (this == other) {
            return true;
        } else if (other == null) {
            return false;
        } else {
            return this.root.getId() == other.root.getId() && this.period.equals(other.period);
        }

    }

    @Override
    public int hashCode() {
        int result = this.root.hashCode();
        result = 31 * result + this.period.hashCode();
        return result;
    }

}