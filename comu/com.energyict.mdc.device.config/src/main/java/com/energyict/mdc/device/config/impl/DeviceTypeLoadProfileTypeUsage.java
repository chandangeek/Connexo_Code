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
import com.energyict.mdc.masterdata.LoadProfileType;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

class DeviceTypeLoadProfileTypeUsage {

    @IsPresent
    private Reference<DeviceType> deviceType = ValueReference.absent();
    @IsPresent
    private Reference<LoadProfileType> loadProfileType = ValueReference.absent();
    private Reference<RegisteredCustomPropertySet> customPropertySet = ValueReference.absent();
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
    DeviceTypeLoadProfileTypeUsage(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    DeviceTypeLoadProfileTypeUsage initialize(DeviceType deviceType, LoadProfileType loadProfileType) {
        this.deviceType.set(deviceType);
        this.loadProfileType.set(loadProfileType);
        return this;
    }

    public DeviceType getDeviceType() {
        return deviceType.get();
    }

    public LoadProfileType getLoadProfileType() {
        return loadProfileType.get();
    }

    public Optional<RegisteredCustomPropertySet> getRegisteredCustomPropertySet() {
        return customPropertySet.getOptional();
    }

    public void setCustomPropertySet(RegisteredCustomPropertySet registeredCustomPropertySet) {
        this.customPropertySet.set(registeredCustomPropertySet);
        update();
    }

    boolean sameLoadProfileType(LoadProfileType loadProfileType) {
        return this.getLoadProfileType().getId() == loadProfileType.getId();
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
        DeviceTypeLoadProfileTypeUsage that = (DeviceTypeLoadProfileTypeUsage) o;
        return this.getDeviceType().getId() == that.getDeviceType().getId() &&
                this.getLoadProfileType().getId() == that.getLoadProfileType().getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceType, loadProfileType);
    }
}