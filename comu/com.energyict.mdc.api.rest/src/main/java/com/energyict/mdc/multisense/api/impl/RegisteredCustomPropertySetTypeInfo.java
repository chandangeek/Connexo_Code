/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.util.HasId;
import com.energyict.mdc.device.config.DeviceType;

public class RegisteredCustomPropertySetTypeInfo {

    private long parentId;
    private long version;
    private HasId parent;
    private RegisteredCustomPropertySet registeredCustomPropertySet;

    private RegisteredCustomPropertySetTypeInfo() {
    }

    public HasId getParent() {
        return parent;
    }

    public long getParentId() {
        return parentId;
    }

    public long getVersion() {
        return version;
    }

    public RegisteredCustomPropertySet getRegisteredCustomPropertySet() {
        return registeredCustomPropertySet;
    }

    public static Builder builder() {
        return new RegisteredCustomPropertySetTypeInfo().new Builder();
    }

    public class Builder {

        private Builder() {
        }

        public Builder from(DeviceType deviceType, RegisteredCustomPropertySet registeredCustomPropertySet) {
            setParentId(deviceType.getId());
            setVersion(deviceType.getVersion());
            setParent(deviceType);
            setRegisteredCustomPropertySet(registeredCustomPropertySet);
            return this;
        }

        public Builder setParentId(long parentId) {
            RegisteredCustomPropertySetTypeInfo.this.parentId = parentId;
            return this;
        }

        public Builder setParent(HasId parent) {
            RegisteredCustomPropertySetTypeInfo.this.parent = parent;
            return this;
        }

        public Builder setRegisteredCustomPropertySet(RegisteredCustomPropertySet registeredCustomPropertySet) {
            RegisteredCustomPropertySetTypeInfo.this.registeredCustomPropertySet = registeredCustomPropertySet;
            return this;
        }

        public Builder setVersion(long version) {
            RegisteredCustomPropertySetTypeInfo.this.version = version;
            return this;
        }

        public RegisteredCustomPropertySetTypeInfo build() {
            return RegisteredCustomPropertySetTypeInfo.this;
        }
    }
}