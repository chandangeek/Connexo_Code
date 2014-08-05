package com.energyict.mdc.masterdata.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.time.UtcInstant;
import com.energyict.mdc.masterdata.RegisterGroup;
import com.energyict.mdc.masterdata.RegisterType;

import javax.inject.Inject;

class RegisterTypeInGroup {
    private final DataModel dataModel;

    @IsPresent
    private Reference<RegisterType> registerType = ValueReference.absent();
    @IsPresent
    private Reference<RegisterGroup> registerGroup = ValueReference.absent();
    private UtcInstant createTime;

    @Inject
    RegisterTypeInGroup(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    RegisterTypeInGroup init(RegisterGroup group , RegisterType registerType) {
        this.registerGroup.set(group);
        this.registerType.set(registerType);
        return this;
    }

    static RegisterTypeInGroup from(DataModel dataModel, RegisterGroup group, RegisterType registerType) {
        return dataModel.getInstance(RegisterTypeInGroup.class).init(group, registerType);
    }

    RegisterType getRegisterType() {
        return registerType.get();
    }

    RegisterGroup getRegisterGroup() {
        return registerGroup.get();
    }

    void persist() {
        dataModel.mapper(RegisterTypeInGroup.class).persist(this);
    }

    void delete() {
        dataModel.mapper(RegisterTypeInGroup.class).remove(this);
    }

}