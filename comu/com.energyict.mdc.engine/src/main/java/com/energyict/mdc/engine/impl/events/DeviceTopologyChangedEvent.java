package com.energyict.mdc.engine.impl.events;

import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.google.common.collect.ImmutableList;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class DeviceTopologyChangedEvent {

    private final String masterDeviceId;
    private final DeviceIdentifier masterDevice;
    private final String slaveIdentifiers;
    private final List<DeviceIdentifier> slaveDevices;

    public DeviceTopologyChangedEvent(DeviceIdentifier masterDeviceIdentifier, Collection<DeviceIdentifier> slaveIdentifiers) {
        super();
        masterDevice = masterDeviceIdentifier;
        this.masterDeviceId = masterDeviceIdentifier.getIdentifier();
        slaveDevices = slaveIdentifiers;
        this.slaveIdentifiers = asString(slaveIdentifiers);
    }

    private String asString(Collection<DeviceIdentifier> slaveIdentifiers) {
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