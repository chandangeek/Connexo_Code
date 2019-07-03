/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.config.SecurityAccessorTypeKeyRenewal;
import com.energyict.mdc.common.device.config.SecurityAccessorTypeOnDeviceType;
import com.energyict.mdc.common.protocol.DeviceMessageId;
import com.energyict.mdc.common.protocol.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


public class SecurityAccessorTypeOnDeviceTypeImpl implements SecurityAccessorTypeOnDeviceType {
    enum Fields {
        DEVICETYPE("deviceType"),
        SECACCTYPE("securityAccessorType"),
        DEFAULTKEY("defaultKey");

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
    private String defaultKey;

    // associations
    private List<SecurityAccessorTypeKeyRenewal> securityAccessorTypeKeyRenewals = new ArrayList<>();
    @SuppressWarnings("unused")
    private String userName;
    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;

    private DeviceMessageSpecificationService deviceMessageSpecificationService;

    private final DataModel dataModel;

    @Inject
    SecurityAccessorTypeOnDeviceTypeImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    SecurityAccessorTypeOnDeviceTypeImpl init(DeviceType deviceType, SecurityAccessorType securityAccessorType) {
        this.deviceType.set(deviceType);
        this.securityAccessorType.set(securityAccessorType);
        return this;
    }

    @Inject
    SecurityAccessorTypeOnDeviceTypeImpl(DeviceMessageSpecificationService deviceMessageSpecificationService, DataModel dataModel) {
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
        this.dataModel = dataModel;
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
    public Optional<DeviceMessageId> getKeyRenewalDeviceMessageId() {
        return securityAccessorTypeKeyRenewals.stream()
                .map(SecurityAccessorTypeKeyRenewal::getKeyRenewalDeviceMessageId)
                .findFirst();
    }

    @Override
    public Optional<DeviceMessageSpec> getKeyRenewalDeviceMessageSpecification() {
        return getKeyRenewalDeviceMessageId()
                .map(deviceMessageId -> this.deviceMessageSpecificationService.findMessageSpecById(deviceMessageId.dbValue()))
                .orElseGet(Optional::empty);
    }

    @Override
    public List<SecurityAccessorTypeKeyRenewal> getKeyRenewalAttributes() {
        return securityAccessorTypeKeyRenewals;
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

    public Optional<String> getDefaultKey() {
        return Optional.ofNullable(defaultKey);
    }

    public void setDefaultKey(String defaultKey) {
        this.defaultKey = defaultKey;
        dataModel.update(this);
    }
}
