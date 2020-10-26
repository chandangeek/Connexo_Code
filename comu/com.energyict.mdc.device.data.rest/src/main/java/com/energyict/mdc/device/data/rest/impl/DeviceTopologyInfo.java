/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.MeteringTranslationService;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.device.data.Device;
import com.elster.jupiter.metering.DefaultState;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.topology.G3Neighbor;
import com.energyict.mdc.device.topology.TopologyTimeline;
import com.energyict.mdc.device.topology.TopologyTimeslice;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceTopologyInfo {
    public long id;
    public String name;
    public String deviceTypeName;
    public String deviceConfigurationName;
    public Long linkingTimeStamp;
    public String serialNumber;
    public long creationTime;
    public String state;
    public G3NodePLCInfo g3NodePLCInfo;

    public static List<DeviceTopologyInfo> from(TopologyTimeline timeline, MeteringTranslationService meteringTranslationService) {
        return timeline.getAllDevices().stream()
                .filter(device -> hasNotEnded(timeline, device))
                .sorted(new DeviceRecentlyAddedComporator(timeline))
                .map(d -> from(d, timeline.mostRecentlyAddedOn(d), meteringTranslationService))
                .collect(Collectors.toList());
    }

    public static DeviceTopologyInfo from(Device device, Optional<Instant> linkingTimeStamp, MeteringTranslationService meteringTranslationService) {
        DeviceTopologyInfo info = new DeviceTopologyInfo();
        info.id = device.getId();
        info.name = device.getName();
        info.deviceTypeName = device.getDeviceType().getName();
        info.deviceConfigurationName = device.getDeviceConfiguration().getName();
        info.serialNumber = device.getSerialNumber();
        info.creationTime = device.getCreateTime().toEpochMilli();
        if (linkingTimeStamp.isPresent() && linkingTimeStamp.get() != Instant.MAX) {
            info.linkingTimeStamp = linkingTimeStamp.get().toEpochMilli();
        }
        info.state = DefaultState.from(device.getState()).map(meteringTranslationService::getDisplayName).orElseGet(device.getState()::getName);
        return info;
    }

    public static DeviceTopologyInfo from(Device device, Optional<Instant> linkingTimeStamp, MeteringTranslationService meteringTranslationService,
                                          Optional<G3Neighbor> g3Neighbor, Thesaurus thesaurus) {
        DeviceTopologyInfo deviceTopologyInfo = DeviceTopologyInfo.from(device, linkingTimeStamp, meteringTranslationService);
        g3Neighbor.ifPresent(g3Neighbor1 -> deviceTopologyInfo.g3NodePLCInfo = G3NodePLCInfo.from(g3Neighbor1, thesaurus));
        return deviceTopologyInfo;
    }

    public static DeviceTopologyInfo from(Device device, Optional<Instant> linkingTimeStamp, DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService,
                                          Optional<G3Neighbor> g3Neighbor, Thesaurus thesaurus) {
        DeviceTopologyInfo deviceTopologyInfo = DeviceTopologyInfo.from(device, linkingTimeStamp, deviceLifeCycleConfigurationService);
        g3Neighbor.ifPresent(g3Neighbor1 -> deviceTopologyInfo.g3NodePLCInfo = G3NodePLCInfo.from(g3Neighbor1, thesaurus));
        return deviceTopologyInfo;
    }

    public static DeviceTopologyInfo from(Device device, Optional<Instant> linkingTimeStamp, DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService) {
        DeviceTopologyInfo info = new DeviceTopologyInfo();
        info.id = device.getId();
        info.name = device.getName();
        info.deviceTypeName = device.getDeviceType().getName();
        info.deviceConfigurationName = device.getDeviceConfiguration().getName();
        info.serialNumber = device.getSerialNumber();
        info.creationTime = device.getCreateTime().toEpochMilli();
        if (linkingTimeStamp.isPresent() && linkingTimeStamp.get() != Instant.MAX) {
            info.linkingTimeStamp = linkingTimeStamp.get().toEpochMilli();
        }
        info.state = DefaultState.from(device.getState()).map(deviceLifeCycleConfigurationService::getDisplayName).orElseGet(device.getState()::getName);
        return info;
    }

    private static class DeviceRecentlyAddedComporator implements Comparator<Device> {

        private TopologyTimeline timeline;
        DeviceRecentlyAddedComporator(TopologyTimeline timeline) {
            this.timeline = timeline;
        }

        @Override
        public int compare(Device d1, Device d2) {
            Optional<Instant> d1AddTime = this.timeline.mostRecentlyAddedOn(d1);
            Optional<Instant> d2AddTime = this.timeline.mostRecentlyAddedOn(d2);
            if (!d1AddTime.isPresent() && !d2AddTime.isPresent()) {
                return 0;
            } else if (!d1AddTime.isPresent() && d2AddTime.isPresent()) {
                return 1;
            } else if (!d2AddTime.isPresent() && d1AddTime.isPresent()) {
                return -1;
            }
            return -1 * d1AddTime.get().compareTo(d2AddTime.get());
        }

    }

    public static boolean hasNotEnded(TopologyTimeline timeline, Device device) {
        List<TopologyTimeslice> x1 = timeline.getSlices()
                .stream()
                .filter(s -> contains(s, device)).collect(Collectors.toList());

        List<TopologyTimeslice> x2 = timeline.getSlices()
                .stream()
                .filter(s -> contains(s, device))
                .sorted((s1, s2) -> s2.getPeriod().lowerEndpoint().compareTo(s1.getPeriod().lowerEndpoint()))
                .collect(Collectors.toList());

        Optional<TopologyTimeslice> first = timeline.getSlices()
                .stream()
                .filter(s -> contains(s, device))
                .sorted((s1, s2) -> s2.getPeriod().lowerEndpoint().compareTo(s1.getPeriod().lowerEndpoint()))
                .findFirst();
        return first.filter(topologyTimeslice -> !topologyTimeslice.getPeriod().hasUpperBound()).isPresent();
    }

    private static boolean contains(TopologyTimeslice timeslice, Device device) {
        return timeslice.getDevices()
                .stream()
                .anyMatch(d -> d.getId() == device.getId());
    }
}
