package com.energyict.mdc.masterdata.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.UtcInstant;
import com.energyict.mdc.masterdata.RegisterGroup;
import com.energyict.mdc.masterdata.RegisterMapping;
import com.energyict.mdc.masterdata.RegisterMappingInGroup;

import javax.inject.Inject;

public class RegisterMappingInGroupImpl implements RegisterMappingInGroup {
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
    RegisterMappingInGroupImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    RegisterMappingInGroupImpl init(RegisterGroup group , RegisterMapping mapping) {
        this.registerGroupId = group.getId();
        this.registerMappingId = mapping.getId();
        this.registerGroup = group;
        this.registerMapping = mapping;
        return this;
    }

    static RegisterMappingInGroupImpl from(DataModel dataModel, RegisterGroup group, RegisterMapping mapping) {
        return dataModel.getInstance(RegisterMappingInGroupImpl.class).init(group, mapping);
    }

    @Override
    public RegisterMapping getRegisterMapping() {
        if (registerMapping == null) {
            registerMapping = dataModel.mapper(RegisterMapping.class).getExisting(registerMappingId);
        }
        return registerMapping;
    }

    @Override
    public RegisterGroup getRegisterGroup() {
        if (registerGroup == null) {
            registerGroup = dataModel.mapper(RegisterGroup.class).getExisting(registerGroupId);
        }
        return registerGroup;
    }

    void persist() {
        dataModel.mapper(RegisterMappingInGroupImpl.class).persist(this);
    }

    public void delete() {
        dataModel.mapper(RegisterMappingInGroupImpl.class).remove(this);
    }
}
