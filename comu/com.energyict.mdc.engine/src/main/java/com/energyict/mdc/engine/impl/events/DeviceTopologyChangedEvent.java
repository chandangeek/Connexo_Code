package com.energyict.mdc.engine.impl.events;

import com.energyict.mdc.protocol.api.LastSeenDateInfo;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DeviceTopologyChangedEvent {

    private final String masterDeviceId;
    private final DeviceIdentifier masterDevice;
    private final String slaveIdentifiers;
    private final List<DeviceIdentifier> slaveDevices;

    public DeviceTopologyChangedEvent(DeviceIdentifier masterDeviceIdentifier, Map<DeviceIdentifier, LastSeenDateInfo> slaveIdentifiers) {
        super();
        masterDevice = masterDeviceIdentifier;
        this.masterDeviceId = masterDeviceIdentifier.getIdentifier();
        slaveDevices = ImmutableList.copyOf(slaveIdentifiers.keySet());
        this.slaveIdentifiers = asString(slaveIdentifiers.keySet());
    }

    private String asString(Set<DeviceIdentifier> slaveIdentifiers) {
        return slaveIdentifiers
                .stream()
                .map(DeviceIdentifier::getIdentifier)
                .collect(Collectors.joining(","));
    }

    public String getMasterDeviceId() {
        return masterDeviceId;
    }

    public String getSlaveIdentifiers() {
        return slaveIdentifiers;
    }

    public DeviceIdentifier getMasterDevice() {
        return masterDevice;
    }

    public List<DeviceIdentifier> getSlaveDevices() {
        return slaveDevices;
    }

}