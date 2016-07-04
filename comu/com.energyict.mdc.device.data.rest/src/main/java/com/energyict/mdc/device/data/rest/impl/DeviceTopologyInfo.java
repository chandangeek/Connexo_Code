package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.energyict.mdc.device.topology.DeviceTopology;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.device.topology.TopologyTimeline;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.Range;

import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceTopologyInfo {
    public long id;
    public String mRID;
    public String deviceTypeName;
    public String deviceConfigurationName;
    public Long linkingTimeStamp;
    public String serialNumber;
    public long creationTime;
    public String state;

    public static List<DeviceTopologyInfo> from(TopologyTimeline timeline, TopologyService topologyService, Clock clock, Thesaurus thesaurus) {
        return timeline.getAllDevices().stream()
                .sorted(new DeviceRecentlyAddedComporator(timeline))
                .map(d -> from(d, timeline.mostRecentlyAddedOn(d), topologyService, clock, thesaurus))
                .collect(Collectors.toList());
    }

    public static List<DeviceTopologyInfo> from(List<Device> devices) {
        return devices.stream().map(DeviceTopologyInfo::from).collect(Collectors.toList());
    }

    public static DeviceTopologyInfo from(Device device, Optional<Instant> addTime, TopologyService topologyService, Clock clock, Thesaurus thesaurus) {
        DeviceTopologyInfo info = new DeviceTopologyInfo();
        info.id =device.getId();
        info.mRID = device.getmRID();
        info.deviceTypeName = device.getDeviceType().getName();
        info.deviceConfigurationName = device.getDeviceConfiguration().getName();
        info.serialNumber = device.getSerialNumber();
        info.creationTime = addTime.orElse(Instant.EPOCH).toEpochMilli();
        if (device.getDeviceType().isDataloggerSlave()) {
            info.linkingTimeStamp = topologyService.findCurrentDataloggerReference(device, clock.instant())
                    .map(dataLoggerReference -> dataLoggerReference.getRange().lowerEndpoint().toEpochMilli())
                    .orElse(null);
        } else {
            Device gateway = topologyService.getPhysicalGateway(device).get();
            DeviceTopology physicalTopology = topologyService.getPhysicalTopology(gateway, Range.atMost(clock.instant()));
            physicalTopology.timelined().mostRecentlyAddedOn(device).ifPresent(instant -> info.linkingTimeStamp = instant.toEpochMilli());
        }
        String key = DefaultState.from(device.getState()).get().getKey();
        info.state = thesaurus.getString(key, key);
        return info;
    }

    public static DeviceTopologyInfo from(Device device) {
        DeviceTopologyInfo info = new DeviceTopologyInfo();
        info.id = device.getId();
        info.mRID = device.getmRID();
        return info;
    }

    private static class DeviceRecentlyAddedComporator implements Comparator<Device>{
        private TopologyTimeline timeline;

        public DeviceRecentlyAddedComporator(TopologyTimeline timeline) {
            this.timeline = timeline;
        }

        @Override
        public int compare(Device d1, Device d2) {
            Optional<Instant> d1AddTime = this.timeline.mostRecentlyAddedOn(d1);
            Optional<Instant> d2AddTime = this.timeline.mostRecentlyAddedOn(d2);
            if (!d1AddTime.isPresent() && !d2AddTime.isPresent()){
                return 0;
            } else if (!d1AddTime.isPresent() && d2AddTime.isPresent()){
                return 1;
            } else if (!d2AddTime.isPresent() && d1AddTime.isPresent()){
                return -1;
            }
            return -1 * d1AddTime.get().compareTo(d2AddTime.get());
        }
    }
}
