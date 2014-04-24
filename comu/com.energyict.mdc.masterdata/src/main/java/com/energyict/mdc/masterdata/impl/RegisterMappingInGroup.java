package com.energyict.mdc.masterdata.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.UtcInstant;
import com.energyict.mdc.masterdata.RegisterGroup;
import com.energyict.mdc.masterdata.RegisterMapping;

import javax.inject.Inject;

class RegisterMappingInGroup {
    // persistent fields
    private long registerGroupId;
    private long registerMappingId;
    @SuppressWarnings("unused")
    private UtcInstant createTime;

    // associations
    private RegisterMapping registerMapping;
    private RegisterGroup registerGroup;
    private final DataModel dataModel;

    @Inject
    RegisterMappingInGroup(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    RegisterMappingInGroup init(RegisterGroup group , RegisterMapping mapping) {
        this.registerGroupId = group.getId();
        this.registerMappingId = mapping.getId();
        this.registerGroup = group;
        this.registerMapping = mapping;
        return this;
    }

    static RegisterMappingInGroup from(DataModel dataModel, RegisterGroup group, RegisterMapping mapping) {
        return dataModel.getInstance(RegisterMappingInGroup.class).init(group, mapping);
    }

    RegisterMapping getRegisterMapping() {
        if (registerMapping == null) {
            registerMapping = dataModel.mapper(RegisterMapping.class).getExisting(registerMappingId);
        }
        return registerMapping;
    }

    RegisterGroup getRegisterGroup() {
        if (registerGroup == null) {
            registerGroup = dataModel.mapper(RegisterGroup.class).getExisting(registerGroupId);
        }
        return registerGroup;
    }

    void persist() {
        dataModel.mapper(RegisterMappingInGroup.class).persist(this);
    }

    void delete() {
        dataModel.mapper(RegisterMappingInGroup.class).remove(this);
    }
}
