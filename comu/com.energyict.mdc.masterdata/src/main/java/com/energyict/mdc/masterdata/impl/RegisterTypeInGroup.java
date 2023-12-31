/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.masterdata.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.common.masterdata.RegisterGroup;
import com.energyict.mdc.common.masterdata.RegisterType;

import java.time.Instant;

class RegisterTypeInGroup {
    @IsPresent
    private Reference<RegisterType> registerType = ValueReference.absent();
    @IsPresent
    private Reference<RegisterGroup> registerGroup = ValueReference.absent();
    @SuppressWarnings("unused")
    private String userName;
    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;

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

}