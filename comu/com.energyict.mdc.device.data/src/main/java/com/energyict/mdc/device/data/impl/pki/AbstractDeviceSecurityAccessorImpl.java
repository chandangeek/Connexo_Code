/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.energyict.mdc.device.data.impl.pki;

import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.SecurityValueWrapper;
import com.elster.jupiter.pki.impl.accessors.SecurityAccessorTypeImpl;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.SecurityAccessor;
import com.energyict.mdc.device.data.KeyAccessorStatus;

import com.google.common.collect.ImmutableMap;

import javax.xml.bind.annotation.XmlElement;
import java.time.Instant;
import java.util.List;
import java.util.Map;

// almost copy-pasted as com.elster.jupiter.pki.impl.accessors.AbstractSecurityAccessorImpl.
// A refactoring towards usage of that class can be attempted
// TODO validate actual value is present, fix newBlaBla() first to take actual value!
public abstract class AbstractDeviceSecurityAccessorImpl<T extends SecurityValueWrapper> implements SecurityAccessor<T> {
    private SecurityManagementService securityManagementService;

    private Reference<SecurityAccessorType> keyAccessorTypeReference = Reference.empty();
    private Reference<Device> deviceReference = Reference.empty();
    private boolean swapped;
    private boolean serviceKey;

    @SuppressWarnings("unused")
    private String userName;
    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;

    public static final Map<String, Class<? extends SecurityAccessor>> IMPLEMENTERS =
            ImmutableMap.of(
                    "C", CertificateAccessorImpl.class,
                    "P", PassphraseAccessorImpl.class,
                    "S", PlainTextSymmetricKeyAccessorImpl.class,
                    "H", HsmSymmetricKeyAccessorImpl.class);

    public AbstractDeviceSecurityAccessorImpl() {
        super();
    }

    protected AbstractDeviceSecurityAccessorImpl(SecurityManagementService securityManagementService) {
        this.securityManagementService = securityManagementService;
    }

    public enum Fields {
        KEY_ACCESSOR_TYPE("keyAccessorTypeReference"),
        DEVICE("deviceReference"),
        SWAPPED("swapped"),
        CERTIFICATE_WRAPPER_ACTUAL("actualCertificate"),
        CERTIFICATE_WRAPPER_TEMP("tempCertificate"),
        SYMM_KEY_WRAPPER_ACTUAL("actualSymmetricKeyWrapperReference"),
        SYMM_KEY_WRAPPER_TEMP("tempSymmetricKeyWrapperReference"),
        PASSPHRASE_WRAPPER_ACTUAL("actualPassphraseWrapperReference"),
        PASSPHRASE_WRAPPER_TEMP("tempPassphraseWrapperReference"),
        SERVICEKEY("serviceKey")
        ;

        private final String fieldName;

        Fields(String fieldName) {
            this.fieldName = fieldName;
        }

        public String fieldName() {
            return fieldName;
        }
    }

    public void init(SecurityAccessorType securityAccessorType, Device device) {
        this.keyAccessorTypeReference.set(securityAccessorType);
        this.deviceReference.set(device);
    }

    @Override
    @XmlElement(type = SecurityAccessorTypeImpl.class)
    public SecurityAccessorType getKeyAccessorTypeReference() {
        return keyAccessorTypeReference.get();
    }

    public void setKeyAccessorTypeReference(SecurityAccessorType securityAccessorType) {
        keyAccessorTypeReference.set(securityAccessorType);
    }

    @Override
    public Device getDevice() {
        return deviceReference.get();
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return securityManagementService.getPropertySpecs(getKeyAccessorTypeReference());
    }

    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public void swapValues() {
        this.swapped=!swapped;
    }

    @Override
    public void clearTempValue() {
        this.swapped=false;
    }

    public abstract void clearActualValue();

    @Override
    public boolean isSwapped() {
        return swapped;
    }

    @Override
    public Instant getModTime() {
        return modTime;
    }

    @Override
    public KeyAccessorStatus getStatus() {
        if (!getActualPassphraseWrapperReference().isPresent() || getActualPassphraseWrapperReference().get().getProperties().containsValue(null) || getActualPassphraseWrapperReference().get().getProperties().size()!=getPropertySpecs().size()) {
            return KeyAccessorStatus.INCOMPLETE;
        }
        return KeyAccessorStatus.COMPLETE;
    }

    @Override
    public boolean isEditable() {
        return true;
    }

    @Override
    public void setServiceKey(boolean serviceKey) {
        this.serviceKey = serviceKey;
    }

    @Override
    public boolean isServiceKey() {
        return this.serviceKey;
    }
}
