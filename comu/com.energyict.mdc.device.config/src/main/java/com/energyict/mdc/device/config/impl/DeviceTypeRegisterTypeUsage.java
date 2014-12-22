package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.masterdata.RegisterType;

import java.time.Instant;

/**
 * Models the fact that a {@link DeviceType} uses a {@link com.energyict.mdc.masterdata.RegisterType}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-30 (08:25)
 */
class DeviceTypeRegisterTypeUsage {
    private Reference<DeviceType> deviceType = ValueReference.absent();
    private Reference<RegisterType> registerType = ValueReference.absent();
    private String userName;
    private long version;
    private Instant createTime;
    private Instant modTime;

    // For orm service only
    DeviceTypeRegisterTypeUsage() {
        super();
    }

    DeviceTypeRegisterTypeUsage(DeviceType deviceType, RegisterType registerType) {
        this();
        this.deviceType.set(deviceType);
        this.registerType.set(registerType);
    }

    public DeviceType getDeviceType() {
        return deviceType.get();
    }

    public RegisterType getRegisterType() {
        return registerType.get();
    }

    public boolean sameRegisterType(RegisterType registerType) {
        return this.getRegisterType().getId() == registerType.getId();
    }

}