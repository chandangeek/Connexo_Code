/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.events;

import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;

import java.util.List;
import java.util.stream.Collectors;

public class DeviceTopologyChangedEvent {

    private final String masterDeviceId;
    private final DeviceIdentifier masterDevice;
    private final String slaveIdentifiers;
    private final List<DeviceIdentifier> slaveDevices;

    public DeviceTopologyChangedEvent(DeviceIdentifier masterDeviceIdentifier, List<DeviceIdentifier> slaveIdentifiers) {
        super();
        masterDevice = masterDeviceIdentifier;
        this.masterDeviceId = masterDeviceIdentifier.getIdentifier();
        slaveDevices = slaveIdentifiers;
        this.slaveIdentifiers = asString(slaveIdentifiers);
    }

    private String asString(List<DeviceIdentifier> slaveIdentifiers) {
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