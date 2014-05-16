package com.energyict.mdc.engine.impl.events;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;

import java.util.Collections;
import java.util.List;

public class DeviceTopologyChangedEvent {

    private final Device master;
    /**
     * A list containing the device identifiers of all attached slave devices.
     */
    private List<DeviceIdentifier> slaveDeviceIdentifiers;

    public DeviceTopologyChangedEvent(Device master, List<DeviceIdentifier>  slaveDeviceIdentifiers) {
        this.master = master;
        this.slaveDeviceIdentifiers = slaveDeviceIdentifiers;
    }

    public Device getMasterDevice() {
        return master;
    }

    public List<DeviceIdentifier> getSlaveDeviceIdentifiers() {
        return Collections.unmodifiableList(this.slaveDeviceIdentifiers);
    }

}