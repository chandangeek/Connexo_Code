package com.energyict.mdc.masterdata.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.time.UtcInstant;
import com.energyict.mdc.masterdata.RegisterGroup;
import com.energyict.mdc.masterdata.RegisterMapping;

import javax.inject.Inject;

class RegisterMappingInGroup {
    private final DataModel dataModel;

    @IsPresent
    private Reference<RegisterMapping> registerMapping = ValueReference.absent();
    @IsPresent
    private Reference<RegisterGroup> registerGroup = ValueReference.absent();
    private UtcInstant createTime;

    @Inject
    RegisterMappingInGroup(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    RegisterMappingInGroup init(RegisterGroup group , RegisterMapping mapping) {
        this.registerGroup.set(group);
        this.registerMapping.set(mapping);
        return this;
    }

    static RegisterMappingInGroup from(DataModel dataModel, RegisterGroup group, RegisterMapping mapping) {
        return dataModel.getInstance(RegisterMappingInGroup.class).init(group, mapping);
    }

    RegisterMapping getRegisterMapping() {
        return registerMapping.get();
    }

    RegisterGroup getRegisterGroup() {
        return registerGroup.get();
    }

    void persist() {
        dataModel.mapper(RegisterMappingInGroup.class).persist(this);
    }

    void delete() {
        dataModel.mapper(RegisterMappingInGroup.class).remove(this);
    }

}