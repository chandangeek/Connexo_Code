package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.device.config.LoadProfileType;
import com.energyict.mdc.device.config.RegisterMapping;

/**
 * Models the fact that a {@link LoadProfileType} uses a {@link RegisterMapping}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-30 (08:25)
 */
class LoadProfileTypeRegisterMappingUsage {
    private Reference<LoadProfileType> loadProfileType = ValueReference.absent();
    private Reference<RegisterMapping> registerMapping = ValueReference.absent();

    // For ORM layer only
    LoadProfileTypeRegisterMappingUsage() {
        super();
    }

    LoadProfileTypeRegisterMappingUsage(LoadProfileType loadProfileType, RegisterMapping registerMapping) {
        this();
        this.loadProfileType.set(loadProfileType);
        this.registerMapping.set(registerMapping);
    }

    public LoadProfileType getLoadProfileType() {
        return loadProfileType.get();
    }

    public RegisterMapping getRegisterMapping() {
        return registerMapping.get();
    }

    public boolean sameRegisterMapping (RegisterMapping registerMapping) {
        return this.getRegisterMapping().getId() == registerMapping.getId();
    }

}