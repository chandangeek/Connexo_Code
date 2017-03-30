/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.device.config.DeviceType;

import java.util.Objects;

class DeviceTypeCustomPropertySetUsageImpl implements DeviceTypeCustomPropertySetUsage {

    public enum Fields {
        DEVICETYPE("deviceType"),
        CUSTOMPROPERTYSET("registeredCustomPropertySet");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    @IsPresent
    private Reference<DeviceType> deviceType = ValueReference.absent();
    @IsPresent
    private Reference<RegisteredCustomPropertySet> registeredCustomPropertySet = ValueReference.absent();

    DeviceTypeCustomPropertySetUsageImpl initialize(DeviceType deviceType, RegisteredCustomPropertySet registeredCustomPropertySet) {
        this.deviceType.set(deviceType);
        this.registeredCustomPropertySet.set(registeredCustomPropertySet);
        return this;
    }

    @Override
    public DeviceType getDeviceType() {
        return deviceType.get();
    }

    @Override
    public RegisteredCustomPropertySet getRegisteredCustomPropertySet() {
        return registeredCustomPropertySet.get();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DeviceTypeCustomPropertySetUsageImpl that = (DeviceTypeCustomPropertySetUsageImpl) o;
        return this.getDeviceType().getId() == that.getDeviceType().getId() &&
                this.getRegisteredCustomPropertySet().getId() == that.getRegisteredCustomPropertySet().getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceType, registeredCustomPropertySet);
    }
}