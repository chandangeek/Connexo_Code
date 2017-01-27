package com.energyict.mdc.engine.impl.events;

import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

public class DeviceTopologyChangedEvent {

    private final String masterDeviceId;
    private final DeviceIdentifier masterDevice;
    private final String slaveIdentifiers;
    private final Collection<DeviceIdentifier> slaveDevices;

    public DeviceTopologyChangedEvent(DeviceIdentifier masterDeviceIdentifier, Collection<DeviceIdentifier> slaveIdentifiers) {
        super();
        this.masterDevice = masterDeviceIdentifier;
        this.masterDeviceId = masterDeviceIdentifier.toString();
        this.slaveDevices = slaveIdentifiers;
        this.slaveIdentifiers = asString(slaveIdentifiers);
    }

    private String asString(Collection<DeviceIdentifier> slaveIdentifiers) {
        return slaveIdentifiers
                .stream()
                .map(DeviceIdentifier::toString)
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

    public Collection<DeviceIdentifier> getSlaveDevices() {
        return Collections.unmodifiableCollection(slaveDevices);
    }

}