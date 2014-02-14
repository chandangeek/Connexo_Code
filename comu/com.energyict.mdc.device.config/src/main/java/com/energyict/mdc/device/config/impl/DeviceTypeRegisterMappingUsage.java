package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.RegisterMapping;

/**
 * Models the fact that a {@link DeviceType} uses a {@link RegisterMapping}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-30 (08:25)
 */
class DeviceTypeRegisterMappingUsage {
    private Reference<DeviceType> deviceType = ValueReference.absent();
    private Reference<RegisterMapping> registerMapping = ValueReference.absent();

    // For orm service only
    DeviceTypeRegisterMappingUsage() {
        super();
    }

    DeviceTypeRegisterMappingUsage(DeviceType deviceType, RegisterMapping registerMapping) {
        this();
        this.deviceType.set(deviceType);
        this.registerMapping.set(registerMapping);
    }

    public DeviceType getDeviceType() {
        return deviceType.get();
    }

    public RegisterMapping getRegisterMapping() {
        return registerMapping.get();
    }

    public boolean sameRegisterMapping (RegisterMapping registerMapping) {
        return this.getRegisterMapping().getId() == registerMapping.getId();
    }

}