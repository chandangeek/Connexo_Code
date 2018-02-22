/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.impl.EventType;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.firmware.SecurityAccessorTypeOnDeviceType;

import com.google.inject.Inject;

import java.time.Instant;
import java.util.Objects;

public class SecurityAccessorTypeOnDeviceTypeImpl implements SecurityAccessorTypeOnDeviceType {
    private final DataModel dataModel;

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

    @Inject
    public SecurityAccessorTypeOnDeviceTypeImpl(DataModel dataModel) {
        this.dataModel = dataModel;
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

    public SecurityAccessorTypeOnDeviceTypeImpl init(DeviceType deviceType, SecurityAccessorType securityAccessorType) {
        this.deviceType.set(deviceType);
        this.securityAccessorType.set(securityAccessorType);
        return this;
    }

    public void delete() {
        dataModel.remove(this);
    }

    public void save() {
        Save.CREATE.save(dataModel, this, Save.Create.class);
    }

    public void update() {
        Save.UPDATE.save(dataModel, this, Save.Create.class);
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

