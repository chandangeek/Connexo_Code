package com.energyict.mdc.masterdata.impl;

import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.LoadProfileTypeRegisterMappingUsage;
import com.energyict.mdc.masterdata.RegisterMapping;

/**
 * Models the fact that a {@link LoadProfileType} uses a {@link RegisterMapping}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-30 (08:25)
 */
class LoadProfileTypeRegisterMappingUsageImpl implements LoadProfileTypeRegisterMappingUsage {
    private Reference<LoadProfileType> loadProfileType = ValueReference.absent();
    private Reference<RegisterMapping> registerMapping = ValueReference.absent();

    // For ORM layer only
    LoadProfileTypeRegisterMappingUsageImpl() {
        super();
    }

    LoadProfileTypeRegisterMappingUsageImpl(LoadProfileType loadProfileType, RegisterMapping registerMapping) {
        this();
        this.loadProfileType.set(loadProfileType);
        this.registerMapping.set(registerMapping);
    }

    @Override
    public LoadProfileType getLoadProfileType() {
        return loadProfileType.get();
    }

    @Override
    public RegisterMapping getRegisterMapping() {
        return registerMapping.get();
    }

    public boolean sameRegisterMapping (RegisterMapping registerMapping) {
        return this.getRegisterMapping().getId() == registerMapping.getId();
    }

}