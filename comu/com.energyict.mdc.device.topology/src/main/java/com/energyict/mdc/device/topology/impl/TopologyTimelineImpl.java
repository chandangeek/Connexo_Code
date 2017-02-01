/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.topology.TopologyTimeline;
import com.energyict.mdc.device.topology.TopologyTimeslice;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Provides an implementation for the {@link TopologyTimeline} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-11-07 (15:00)
 */
public class TopologyTimelineImpl implements TopologyTimeline {

    private final List<TopologyTimeslice> slices = new ArrayList<>();
    private final Map<Long, Device> devicesById = new HashMap<>();

    static TopologyTimelineImpl empty() {
        return new TopologyTimelineImpl();
    }

    private TopologyTimelineImpl() {
        super();
    }

    static TopologyTimelineImpl merge(List<? extends ServerTopologyTimeslice> slices) {
        TopologyTimelineImpl timeline = new TopologyTimelineImpl();
        timeline.mergeAndAdd(slices);
        return timeline;
    }

    private void mergeAndAdd(List<? extends ServerTopologyTimeslice> slices) {
        TopologyTimesliceMerger merger = new TopologyTimesliceMerger();
        slices
                .stream()
                .map(ServerTopologyTimeslice::asCompleteTimeslice)
                .forEach(merger::add);
        merger.getEntries().stream().forEach(this::add);
    }

    static TopologyTimelineImpl forAll(List<? extends ServerTopologyTimeslice> slices) {
        TopologyTimelineImpl timeline = new TopologyTimelineImpl();
        timeline.addAndSort(slices);
        return timeline;
    }

    private void addAndSort(List<? extends ServerTopologyTimeslice> slices) {
        this.slices.addAll(slices);
        Collections.sort(this.slices, (ts1, ts2) -> ts1.getPeriod().lowerEndpoint().compareTo(ts2.getPeriod().lowerEndpoint()));
    }

    private void add(CompleteTopologyTimesliceImpl timeslice) {
        this.slices.add(timeslice);
        timeslice.getDevices()
                .stream()
                .forEach(d -> this.devicesById.put(d.getId(), d));
    }

    @Override
    public Set<Device> getAllDevices() {
        return new HashSet<>(this.devicesById.values());
    }

    @Override
    public List<TopologyTimeslice> getSlices() {
        return Collections.unmodifiableList(this.slices);
    }

    @Override
    public Optional<Instant> mostRecentlyAddedOn(Device device) {
        return this.slices
                .stream()
                .filter(s -> this.contains(s, device))
                .findFirst()
                .map(s -> s.getPeriod().lowerEndpoint());
    }

    @Override
    public Optional<Instant> addedFirstOn(Device device) {
        List<TopologyTimeslice> slicesThatContainTheDevice = this.slices
                .stream()
                .filter(s -> this.contains(s, device))
                .collect(Collectors.toList());
        if (slicesThatContainTheDevice.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(slicesThatContainTheDevice.get(slicesThatContainTheDevice.size() - 1).getPeriod().lowerEndpoint());
        }
    }

    private boolean contains(TopologyTimeslice timeslice, Device device) {
        return timeslice.getDevices()
                .stream()
                .anyMatch(d -> d.getId() == device.getId());
    }

}
