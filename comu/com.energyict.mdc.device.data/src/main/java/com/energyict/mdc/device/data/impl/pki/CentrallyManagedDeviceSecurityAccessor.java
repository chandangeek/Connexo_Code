/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.pki;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.SecurityValueWrapper;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.KeyAccessorStatus;
import com.energyict.mdc.common.device.data.SecurityAccessor;
import com.energyict.mdc.device.data.CertificateAccessor;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public abstract class CentrallyManagedDeviceSecurityAccessor<T extends SecurityValueWrapper> implements SecurityAccessor<T> {
    private final Thesaurus thesaurus;
    private final Device device;
    private final com.elster.jupiter.pki.SecurityAccessor<T> defaultValue;

    private CentrallyManagedDeviceSecurityAccessor(Thesaurus thesaurus, Device device, com.elster.jupiter.pki.SecurityAccessor<T> defaultValue) {
        this.thesaurus = thesaurus;
        this.device = device;
        this.defaultValue = defaultValue;
    }

    public static <T extends SecurityValueWrapper> SecurityAccessor<T> of(Thesaurus thesaurus, Device device, com.elster.jupiter.pki.SecurityAccessor<T> defaultValue) {
        switch (defaultValue.getSecurityAccessorType().getKeyType().getCryptographicType()) {
            case Certificate:
            case ClientCertificate:
            case TrustedCertificate:
                return (SecurityAccessor<T>) new CentrallyManagedCertificateAccessor(thesaurus, device, (com.elster.jupiter.pki.SecurityAccessor<CertificateWrapper>) defaultValue);
            default:
                throw new UnsupportedOperationException("Default values are only supported for certificate accessor type.");
        }
    }

    @Override
    public Device getDevice() {
        return device;
    }

    @Override
    public KeyAccessorStatus getStatus() {
        return KeyAccessorStatus.COMPLETE;
    }

    @Override
    public SecurityAccessorType getSecurityAccessorType() {
        return defaultValue.getSecurityAccessorType();
    }

    @Override
    public Optional<T> getActualValue() {
        return defaultValue.getActualValue();
    }

    @Override
    public void setActualValue(T newValueWrapper) {
        throw new UnmanageableSecurityAccessorException(thesaurus, defaultValue.getSecurityAccessorType());
    }

    @Override
    public Optional<T> getTempValue() {
        return defaultValue.getTempValue();
    }

    @Override
    public void setTempValue(T newValueWrapper) {
        throw new UnmanageableSecurityAccessorException(thesaurus, defaultValue.getSecurityAccessorType());
    }

    @Override
    public void renew() {
        throw new UnmanageableSecurityAccessorException(thesaurus, defaultValue.getSecurityAccessorType());
    }

    @Override
    public void swapValues() {
        throw new UnmanageableSecurityAccessorException(thesaurus, defaultValue.getSecurityAccessorType());
    }

    @Override
    public void clearTempValue() {
        throw new UnmanageableSecurityAccessorException(thesaurus, defaultValue.getSecurityAccessorType());
    }

    @Override
    public void clearActualValue() {
        throw new UnmanageableSecurityAccessorException(thesaurus, defaultValue.getSecurityAccessorType());
    }

    @Override
    public void save() {
        throw new UnmanageableSecurityAccessorException(thesaurus, defaultValue.getSecurityAccessorType());
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return defaultValue.getPropertySpecs();
    }

    @Override
    public void delete() {
        throw new UnmanageableSecurityAccessorException(thesaurus, defaultValue.getSecurityAccessorType());
    }

    @Override
    public long getVersion() {
        return -1;
    }

    @Override
    public boolean isSwapped() {
        return defaultValue.isSwapped();
    }

    @Override
    public Instant getModTime() {
        return defaultValue.getModTime();
    }

    @Override
    public boolean isEditable() {
        return false;
    }

    @Override
    public void setServiceKey(boolean serviceKey) {
        throw new UnmanageableSecurityAccessorException(thesaurus, defaultValue.getSecurityAccessorType());
    }

    @Override
    public boolean isServiceKey() {
        return false;
    }

    private static class CentrallyManagedCertificateAccessor extends CentrallyManagedDeviceSecurityAccessor<CertificateWrapper> implements CertificateAccessor {
        CentrallyManagedCertificateAccessor(Thesaurus thesaurus, Device device, com.elster.jupiter.pki.SecurityAccessor<CertificateWrapper> defaultValue) {
            super(thesaurus, device, defaultValue);
        }
    }
}
