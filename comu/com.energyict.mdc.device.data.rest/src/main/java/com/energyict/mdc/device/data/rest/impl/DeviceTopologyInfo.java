package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.topology.TopologyTimeline;

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

    public static List<DeviceTopologyInfo> from(TopologyTimeline timeline, DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService) {
        return timeline.getAllDevices().stream()
                .sorted(new DeviceRecentlyAddedComporator(timeline))
                .map(d -> from(d, timeline.mostRecentlyAddedOn(d), deviceLifeCycleConfigurationService))
                .collect(Collectors.toList());
    }

    public static DeviceTopologyInfo from(Device device, Optional<Instant> linkingTimeStamp, DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService) {
        DeviceTopologyInfo info = new DeviceTopologyInfo();
        info.id = device.getId();
        info.name = device.getName();
        info.deviceTypeName = device.getDeviceType().getName();
        info.deviceConfigurationName = device.getDeviceConfiguration().getName();
        info.serialNumber = device.getSerialNumber();
        info.creationTime = device.getCreateTime().toEpochMilli();
        if (linkingTimeStamp.isPresent()) {
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
}
