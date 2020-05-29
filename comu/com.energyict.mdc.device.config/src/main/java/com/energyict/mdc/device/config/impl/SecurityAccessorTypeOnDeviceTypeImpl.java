/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.energyict.mdc.common.device.config.DeviceSecurityAccessorType;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class SecurityAccessorTypeOnDeviceTypeImpl implements SecurityAccessorTypeOnDeviceType {
    enum Fields {
        DEVICETYPE("deviceType"),
        SECACCTYPE("securityAccessorType"),
        WRAPPINGSECACCTYPE("wrappingSecurityAccessorType"),
        KEYRENEWALMESSAGEID("keyRenewalMessageIdIdDbValue"),
        SERVICEKEYRENEWALMSGID("serviceKeyRenewalMessageIdDbValue"),
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

    private Reference<SecurityAccessorType> wrappingSecurityAccessorType = Reference.empty();
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private Reference<SecurityAccessorType> securityAccessorType = Reference.empty();
    @Size(max = Table.MAX_STRING_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String defaultKey;

    // associations
    private List<SecurityAccessorTypeKeyRenewal> securityAccessorTypeKeyRenewals = new ArrayList<>();
    private long keyRenewalMessageIdIdDbValue;
    private long serviceKeyRenewalMessageIdDbValue;
    @SuppressWarnings("unused")
    private String userName;
    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;

    private final DataModel dataModel;
    private DeviceMessageSpecificationService deviceMessageSpecificationService;
    private final Thesaurus thesaurus;

    SecurityAccessorTypeOnDeviceTypeImpl init(DeviceType deviceType, DeviceSecurityAccessorType securityAccessorType) {
        this.deviceType.set(deviceType);
        setWrappingAccessor(securityAccessorType);
        if (securityAccessorType != null) {
            this.securityAccessorType.set(securityAccessorType.getSecurityAccessor());
        }
        return this;
    }

    @Inject
    SecurityAccessorTypeOnDeviceTypeImpl(DataModel dataModel, DeviceMessageSpecificationService deviceMessageSpecificationService, Thesaurus thesaurus) {
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
        this.thesaurus = thesaurus;
        this.dataModel = dataModel;
    }

    @Override
    public DeviceType getDeviceType() {
        return deviceType.orNull();
    }

    @Override
    public DeviceSecurityAccessorType getDeviceSecurityAccessorType() {
        return new DeviceSecurityAccessorType(wrappingSecurityAccessorType.orNull(), securityAccessorType.orNull());
    }

    @Override
    public void setWrappingSecurityAccessor(Optional<SecurityAccessorType> deviceSecurityAccessorType) {
        if (deviceSecurityAccessorType.isPresent()) {
            this.wrappingSecurityAccessorType.set(deviceSecurityAccessorType.get());
        } else {
            wrappingSecurityAccessorType.setNull();
        }
    }

    @Override
    public SecurityAccessorType getSecurityAccessorType() {
        return securityAccessorType.orNull();
    }

    @Override
    public Optional<DeviceMessageId> getKeyRenewalDeviceMessageId() {
        return Stream.of(DeviceMessageId.values())
                .filter(deviceMessage -> deviceMessage.dbValue() == this.keyRenewalMessageIdIdDbValue)
                .findFirst();
    }

    @Override
    public Optional<DeviceMessageId> getServiceKeyRenewalDeviceMessageId() {
        return DeviceMessageId.find(this.serviceKeyRenewalMessageIdDbValue);
    }

    @Override
    public Optional<DeviceMessageSpec> getKeyRenewalDeviceMessageSpecification() {
        return getKeyRenewalDeviceMessageId()
                .map(deviceMessageId -> this.deviceMessageSpecificationService.findMessageSpecById(deviceMessageId.dbValue()))
                .orElseGet(Optional::empty);
    }

    @Override
    public Optional<DeviceMessageSpec> getServiceKeyRenewalDeviceMessageSpecification() {
            return getServiceKeyRenewalDeviceMessageId()
                    .map(deviceMessageId -> this.deviceMessageSpecificationService.findMessageSpecById(deviceMessageId.dbValue()))
                    .orElseGet(Optional::empty);
    }

    @Override
    public List<SecurityAccessorTypeKeyRenewal> getKeyRenewalAttributes() {
        return securityAccessorTypeKeyRenewals.stream().filter(a -> !a.isServiceKey()).collect(Collectors.toList());
    }

    @Override
    public void resetKeyRenewal() {
        keyRenewalMessageIdIdDbValue = 0;
        serviceKeyRenewalMessageIdDbValue = 0;
        securityAccessorTypeKeyRenewals.clear();
        save();
    }

    @Override
    public List<SecurityAccessorTypeKeyRenewal> getServiceKeyRenewalAttributes() {
        return securityAccessorTypeKeyRenewals.stream().filter(a -> a.isServiceKey()).collect(Collectors.toList());
    }

    private void setWrappingAccessor(DeviceSecurityAccessorType securityAccessorType) {
        if (securityAccessorType != null && securityAccessorType.getWrappingSecurityAccessor() != null && securityAccessorType.getWrappingSecurityAccessor().isPresent()) {
            this.wrappingSecurityAccessorType.set(securityAccessorType.getWrappingSecurityAccessor().get());
        }
    }

    @Override
    public KeyRenewalBuilder newKeyRenewalBuilder(DeviceMessageId deviceMessageId, DeviceMessageId serviceDeviceMessageId) {
        keyRenewalMessageIdIdDbValue = deviceMessageId != null ? deviceMessageId.dbValue() : 0;
        serviceKeyRenewalMessageIdDbValue = serviceDeviceMessageId != null ? serviceDeviceMessageId.dbValue() : 0;
        securityAccessorTypeKeyRenewals.clear();
        return new InternalKeyRenewalBuilder(this);
    }

    @Override
    public boolean isRenewalConfigured() {
        // not sure about this ... hell knows
        return keyRenewalMessageIdIdDbValue != 0;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj
                || obj instanceof SecurityAccessorTypeOnDeviceTypeImpl
                && getDeviceType().equals(((SecurityAccessorTypeOnDeviceTypeImpl) obj).getDeviceType())
                && getDeviceSecurityAccessorType().equals(((SecurityAccessorTypeOnDeviceTypeImpl) obj).getDeviceSecurityAccessorType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDeviceType(), getDeviceSecurityAccessorType());
    }

    protected Thesaurus getThesaurus() {
        return thesaurus;
    }

    protected DataModel getDataModel() {
        return dataModel;
    }

    protected void save() {
        Save.UPDATE.save(this.dataModel, this, Save.Update.class);
        //securityAccessorTypeKeyRenewals.forEach(securityAccessorTypeKeyRenewal -> ((SecurityAccessorTypeKeyRenewalImpl)securityAccessorTypeKeyRenewal).save());
    }

    private class InternalKeyRenewalBuilder implements KeyRenewalBuilder {

        private final SecurityAccessorTypeOnDeviceTypeImpl securityAccessorTypeOnDeviceType;

        private InternalKeyRenewalBuilder(SecurityAccessorTypeOnDeviceTypeImpl securityAccessorTypeOnDeviceType) {
            this.securityAccessorTypeOnDeviceType = securityAccessorTypeOnDeviceType;
        }

        @Override
        public InternalKeyRenewalBuilder addProperty(String key, Object value, boolean isServiceKey) {
            SecurityAccessorTypeKeyRenewalImpl securityAccessorTypeKeyRenewal = securityAccessorTypeOnDeviceType.getDataModel()
                    .getInstance(SecurityAccessorTypeKeyRenewalImpl.class)
                    .init(securityAccessorTypeOnDeviceType.getDeviceType(), securityAccessorTypeOnDeviceType.getSecurityAccessorType());
            securityAccessorTypeKeyRenewal.setName(key);
            securityAccessorTypeKeyRenewal.setValue(value.toString());
            securityAccessorTypeKeyRenewal.setServiceKey(isServiceKey);
            securityAccessorTypeKeyRenewals.add(securityAccessorTypeKeyRenewal);
            return this;
        }

        @Override
        public SecurityAccessorTypeOnDeviceType add() {
            this.securityAccessorTypeOnDeviceType.save();
            return this.securityAccessorTypeOnDeviceType;
        }
    }

    public Optional<String> getDefaultKey() {
        return Optional.ofNullable(defaultKey);
    }

    public void setDefaultKey(String defaultKey) {
        this.defaultKey = defaultKey;
        dataModel.update(this);
    }
}
