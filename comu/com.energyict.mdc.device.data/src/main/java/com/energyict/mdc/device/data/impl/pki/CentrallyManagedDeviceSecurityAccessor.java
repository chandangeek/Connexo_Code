/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.pki;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.SecurityValueWrapper;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.device.data.CertificateAccessor;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.KeyAccessorStatus;
import com.energyict.mdc.device.data.SecurityAccessor;

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
        switch (defaultValue.getKeyAccessorType().getKeyType().getCryptographicType()) {
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
    public SecurityAccessorType getKeyAccessorType() {
        return defaultValue.getKeyAccessorType();
    }

    @Override
    public Optional<T> getActualValue() {
        return defaultValue.getActualValue();
    }

    @Override
    public void setActualValue(T newWrapperValue) {
        throw new UnmanageableSecurityAccessorException(thesaurus, defaultValue.getKeyAccessorType());
    }

    @Override
    public Optional<T> getTempValue() {
        return defaultValue.getTempValue();
    }

    @Override
    public void setTempValue(T newValueWrapper) {
        throw new UnmanageableSecurityAccessorException(thesaurus, defaultValue.getKeyAccessorType());
    }

    @Override
    public void renew() {
        throw new UnmanageableSecurityAccessorException(thesaurus, defaultValue.getKeyAccessorType());
    }

    @Override
    public void swapValues() {
        throw new UnmanageableSecurityAccessorException(thesaurus, defaultValue.getKeyAccessorType());
    }

    @Override
    public void clearTempValue() {
        throw new UnmanageableSecurityAccessorException(thesaurus, defaultValue.getKeyAccessorType());
    }

    @Override
    public void clearActualValue() {
        throw new UnmanageableSecurityAccessorException(thesaurus, defaultValue.getKeyAccessorType());
    }

    @Override
    public void save() {
        throw new UnmanageableSecurityAccessorException(thesaurus, defaultValue.getKeyAccessorType());
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return defaultValue.getPropertySpecs();
    }

    @Override
    public void delete() {
        throw new UnmanageableSecurityAccessorException(thesaurus, defaultValue.getKeyAccessorType());
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
    public void setServiceKey(Boolean serviceKey) {
        throw new UnmanageableSecurityAccessorException(thesaurus, defaultValue.getKeyAccessorType());
    }

    @Override
    public boolean getServiceKey() {
        throw new UnmanageableSecurityAccessorException(thesaurus, defaultValue.getKeyAccessorType());
    }

    private static class CentrallyManagedCertificateAccessor extends CentrallyManagedDeviceSecurityAccessor<CertificateWrapper> implements CertificateAccessor {
        CentrallyManagedCertificateAccessor(Thesaurus thesaurus, Device device, com.elster.jupiter.pki.SecurityAccessor<CertificateWrapper> defaultValue) {
            super(thesaurus, device, defaultValue);
        }
    }
}
