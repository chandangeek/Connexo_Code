/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.masterdata.RegisterType;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

class DeviceTypeRegisterTypeUsage {

    @IsPresent
    private Reference<DeviceType> deviceType = ValueReference.absent();
    @IsPresent
    private Reference<RegisterType> registerType = ValueReference.absent();
    private Reference<RegisteredCustomPropertySet> registeredCustomPropertySet = ValueReference.absent();
    private DataModel dataModel;
    @SuppressWarnings("unused") // Managed by ORM
    private String userName;
    @SuppressWarnings("unused") // Managed by ORM
    private long version;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant createTime;
    @SuppressWarnings("unused") // Managed by ORM
    private Instant modTime;

    @Inject
    DeviceTypeRegisterTypeUsage(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    DeviceTypeRegisterTypeUsage initialize(DeviceType deviceType, RegisterType registerType) {
        this.deviceType.set(deviceType);
        this.registerType.set(registerType);
        return this;
    }

    public DeviceType getDeviceType() {
        return deviceType.get();
    }

    public RegisterType getRegisterType() {
        return registerType.get();
    }

    public Optional<RegisteredCustomPropertySet> getRegisteredCustomPropertySet() {
        return registeredCustomPropertySet.getOptional();
    }

    public void setCustomPropertySet(RegisteredCustomPropertySet registeredCustomPropertySet) {
        this.registeredCustomPropertySet.set(registeredCustomPropertySet);
        update();
    }

    boolean sameRegisterType(RegisterType registerType) {
        return this.getRegisterType().getId() == registerType.getId();
    }

    public void update() {
        dataModel.update(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DeviceTypeRegisterTypeUsage that = (DeviceTypeRegisterTypeUsage) o;
        return this.getDeviceType().getId() == that.getDeviceType().getId() &&
                this.getRegisterType().getId() == that.getRegisterType().getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceType, registerType);
    }
}