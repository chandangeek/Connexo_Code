package com.energyict.mdc.engine.impl;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;

/**
* Copyrights EnergyICT
* Date: 8/1/14
* Time: 8:50 AM
*/
public final class DeviceIdentifierForAlreadyKnownDevice implements DeviceIdentifier<Device> {

    private final Device device;

    public DeviceIdentifierForAlreadyKnownDevice(Device device) {
        this.device = device;
    }

    @Override
    public String getIdentifier() {
        return this.device.getmRID();
    }

    @Override
    public Device findDevice() {
        return this.device;
    }
}
