package com.energyict.mdc.engine.impl.events;

import com.elster.jupiter.util.Holder;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;
import com.google.common.collect.ImmutableList;

import java.util.List;

import static com.elster.jupiter.util.HolderBuilder.first;

public class DeviceTopologyChangedEvent {

    private final String masterDeviceId;
    private final DeviceIdentifier masterDevice;
    private final String slaveIdentifiers;
    private final List<DeviceIdentifier> slaveDevices;

    public DeviceTopologyChangedEvent(DeviceIdentifier masterDeviceIdentifier, List<DeviceIdentifier> slaveIdentifiers) {
        super();
        masterDevice = masterDeviceIdentifier;
        this.masterDeviceId = masterDeviceIdentifier.getIdentifier();
        slaveDevices = ImmutableList.copyOf(slaveIdentifiers);
        this.slaveIdentifiers = asString(slaveIdentifiers);
    }

    private String asString(List<DeviceIdentifier> slaveIdentifiers) {
        Holder<String> separator = first("").andThen(",");
        StringBuilder builder = new StringBuilder();
        for (DeviceIdentifier slaveIdentifier : slaveIdentifiers) {
            builder.append(separator.get()).append(slaveIdentifier.getIdentifier());
        }
        return builder.toString();
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