package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.masterdata.LoadProfileType;

import java.time.Instant;

/**
 * Models the fact that a {@link DeviceType} uses a {@link LoadProfileType}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-30 (08:25)
 */
class DeviceTypeLoadProfileTypeUsage {
    private Reference<DeviceType> deviceType = ValueReference.absent();
    private Reference<LoadProfileType> loadProfileType = ValueReference.absent();
    private String userName;
    private long version;
    private Instant createTime;
    private Instant modTime;

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