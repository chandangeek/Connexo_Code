/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.associations.IsPresent;
import javax.validation.constraints.Size;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.SecurityAccessorTypeOnDeviceType;
import com.energyict.mdc.device.config.events.EventType;
import com.energyict.mdc.device.config.exceptions.CannotDeleteBecauseStillInUseException;

import java.time.Instant;
import java.util.Objects;
import com.elster.jupiter.orm.Table;
import javax.inject.Inject;
import com.elster.jupiter.orm.DataModel;

import static javax.swing.text.StyleConstants.Size;

public class SecurityAccessorTypeOnDeviceTypeImpl implements SecurityAccessorTypeOnDeviceType {
    enum Fields {
        DEVICETYPE("deviceType"),
        SECACCTYPE("securityAccessorType"),
        DEFAULTKEY("defaultkey");

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
    @Size(max = Table.MAX_STRING_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String defaultkey;

    @SuppressWarnings("unused")
    private String userName;
    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;

    private DataModel dataModel;

    @Inject
    SecurityAccessorTypeOnDeviceTypeImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

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

    public String getDefaultKey() {
        return defaultkey;
    }

    public void setDefaultKey(String defaultkey) {
        this.defaultkey = defaultkey;
        dataModel.update(this);
    }
}
