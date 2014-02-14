package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileType;

/**
 * Models the fact that a {@link DeviceType} uses a {@link LoadProfileType}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-30 (08:25)
 */
class DeviceTypeLoadProfileTypeUsage {
    private Reference<DeviceType> deviceType = ValueReference.absent();
    private Reference<LoadProfileType> loadProfileType = ValueReference.absent();

    // For orm service only
    DeviceTypeLoadProfileTypeUsage() {
        super();
    }

    DeviceTypeLoadProfileTypeUsage(DeviceType deviceType, LoadProfileType loadProfileType) {
        this();
        this.deviceType.set(deviceType);
        this.loadProfileType.set(loadProfileType);
    }

    public DeviceType getDeviceType() {
        return deviceType.get();
    }

    public LoadProfileType getLoadProfileType() {
        return loadProfileType.get();
    }

    public boolean sameLoadProfileType (LoadProfileType loadProfileType) {
        return this.getLoadProfileType().getId() == loadProfileType.getId();
    }

}