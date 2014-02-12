package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.RegisterMapping;

/**
 * Models the fact that a {@link DeviceType} uses a {@link RegisterMapping}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-30 (08:25)
 */
class DeviceTypeRegisterMappingUsage {
    DeviceType deviceType;
    RegisterMapping registerMapping;

    // For orm service only
    DeviceTypeRegisterMappingUsage() {
        super();
    }

    DeviceTypeRegisterMappingUsage(DeviceType deviceType, RegisterMapping registerMapping) {
        this();
        this.deviceType = deviceType;
        this.registerMapping = registerMapping;
    }

}