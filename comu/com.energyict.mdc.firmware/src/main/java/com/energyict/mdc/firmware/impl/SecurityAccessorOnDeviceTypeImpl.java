/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.pki.SecurityAccessor;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.firmware.SecurityAccessorOnDeviceType;

import com.google.inject.Inject;

import java.time.Instant;
import java.util.Objects;

public class SecurityAccessorOnDeviceTypeImpl implements SecurityAccessorOnDeviceType {
    private final DataModel dataModel;

    enum Fields {
        DEVICETYPE("deviceType"),
        SECACCESSOR("securityAccessor");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String fieldName() {
            return javaFieldName;
        }
    }

    @Inject
    public SecurityAccessorOnDeviceTypeImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private final Reference<DeviceType> deviceType = Reference.empty();
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private final Reference<SecurityAccessor> securityAccessor = Reference.empty();
    @SuppressWarnings("unused")
    private String userName;
    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;

    @Override
    public SecurityAccessorOnDeviceType init(DeviceType deviceType, SecurityAccessor securityAccessor) {
        this.deviceType.set(deviceType);
        this.securityAccessor.set(securityAccessor);
        return this;
    }

    @Override
    public void delete() {
        dataModel.remove(this);
    }

    @Override
    public void save() {
        Save.CREATE.save(dataModel, this, Save.Create.class);
    }

    @Override
    public void update() {
        Save.UPDATE.save(dataModel, this, Save.Create.class);
    }

    @Override
    public DeviceType getDeviceType() {
        return deviceType.orNull();
    }

    @Override
    public SecurityAccessor getSecurityAccessor() {
        return securityAccessor.orNull();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj
                || obj instanceof SecurityAccessorOnDeviceTypeImpl
                && getDeviceType().equals(((SecurityAccessorOnDeviceTypeImpl) obj).getDeviceType())
                && getSecurityAccessor().equals(((SecurityAccessorOnDeviceTypeImpl) obj).getSecurityAccessor());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDeviceType(), getSecurityAccessor());
    }
}

