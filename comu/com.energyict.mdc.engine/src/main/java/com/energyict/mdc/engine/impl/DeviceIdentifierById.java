package com.energyict.mdc.engine.impl;

import com.energyict.mdc.common.NotFoundException;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;

/**
 * Provides an implementation for the {@link DeviceIdentifier} interface
 * that uses a {@link com.energyict.mdc.protocol.api.device.BaseDevice}'s database identifier.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-16 (15:10)
 */
public final class DeviceIdentifierById implements DeviceIdentifier<Device> {

    private final long id;
    private final DeviceService deviceService;
    private Device device;

    public DeviceIdentifierById(long id, DeviceService deviceService) {
        super();
        this.id = id;
        this.deviceService = deviceService;
    }

    // used for reflection
    public DeviceIdentifierById(String id, DeviceService deviceService) {
        super();
        this.deviceService = deviceService;
        this.id = Long.parseLong(id);
    }

    @Override
    public Device findDevice() {
        // lazyload the device
        if (this.device == null) {
            this.device = this.deviceService.findDeviceById(this.id);
            if (device == null) {
                throw new NotFoundException("Device with id " + this.id + " not found");
            }
        }
        return device;
    }

    @Override
    public String toString() {
        return "id " + this.id;
    }

    @Override
    public String getIdentifier() {
        return String.valueOf(this.id);
    }

    @Override
    public boolean equals (Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        DeviceIdentifierById that = (DeviceIdentifierById) other;
        return id == that.id;
    }

    @Override
    public int hashCode () {
        return Long.valueOf(this.id).hashCode();
    }

}