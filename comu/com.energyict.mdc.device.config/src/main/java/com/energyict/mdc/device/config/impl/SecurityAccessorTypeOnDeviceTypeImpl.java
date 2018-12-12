/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.SecurityAccessorTypeOnDeviceType;

import java.time.Instant;
import java.util.Objects;

public class SecurityAccessorTypeOnDeviceTypeImpl implements SecurityAccessorTypeOnDeviceType {
    enum Fields {
        DEVICETYPE("deviceType"),
        SECACCTYPE("securityAccessorType");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String fieldName() {
            return javaFieldName;
        }
    }

    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private Reference<DeviceType> deviceType = Reference.empty();
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private Reference<SecurityAccessorType> securityAccessorType = Reference.empty();
    @SuppressWarnings("unused")
    private String userName;
    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;

    SecurityAccessorTypeOnDeviceTypeImpl init(DeviceType deviceType, SecurityAccessorType securityAccessorType) {
        this.deviceType.set(deviceType);
        this.securityAccessorType.set(securityAccessorType);
        return this;
    }

    @Override
    public DeviceType getDeviceType() {
        return deviceType.orNull();
    }

    @Override
    public SecurityAccessorType getSecurityAccessorType() {
        return securityAccessorType.orNull();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj
                || obj instanceof SecurityAccessorTypeOnDeviceTypeImpl
                && getDeviceType().equals(((SecurityAccessorTypeOnDeviceTypeImpl) obj).getDeviceType())
                && getSecurityAccessorType().equals(((SecurityAccessorTypeOnDeviceTypeImpl) obj).getSecurityAccessorType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDeviceType(), getSecurityAccessorType());
    }
}
