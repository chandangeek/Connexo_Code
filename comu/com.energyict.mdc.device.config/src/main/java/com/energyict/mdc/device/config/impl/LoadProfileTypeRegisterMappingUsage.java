package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.device.config.LoadProfileType;
import com.energyict.mdc.device.config.RegisterMapping;

/**
 * Models the fact that a {@link LoadProfileType} uses a {@link RegisterMapping}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-30 (08:25)
 */
class LoadProfileTypeRegisterMappingUsage {
    LoadProfileType loadProfileType;
    RegisterMapping registerMapping;

    // For ORM layer only
    LoadProfileTypeRegisterMappingUsage() {
        super();
    }

    LoadProfileTypeRegisterMappingUsage(LoadProfileType loadProfileType, RegisterMapping registerMapping) {
        this();
        this.loadProfileType = loadProfileType;
        this.registerMapping = registerMapping;
    }

}