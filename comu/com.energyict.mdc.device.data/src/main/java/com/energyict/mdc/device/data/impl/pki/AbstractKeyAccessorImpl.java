/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.energyict.mdc.device.data.impl.pki;

import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.pki.KeyAccessorType;
import com.elster.jupiter.pki.PkiService;
import com.elster.jupiter.pki.SecurityValueWrapper;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.KeyAccessor;
import com.energyict.mdc.device.data.KeyAccessorStatus;

import com.google.common.collect.ImmutableMap;

import java.time.Instant;
import java.util.List;
import java.util.Map;


// TODO validate actual value is present, fix newBlaBla() first to take actual value!
public abstract class AbstractKeyAccessorImpl<T extends SecurityValueWrapper> implements KeyAccessor<T> {
    private final PkiService pkiService;

    private Reference<KeyAccessorType> keyAccessorTypeReference = Reference.empty();
    private Reference<Device> deviceReference = Reference.empty();
    private boolean swapped=false;

    @SuppressWarnings("unused")
    private String userName;
    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;

    public static final Map<String, Class<? extends KeyAccessor>> IMPLEMENTERS =
            ImmutableMap.of(
                    "C", CertificateAccessorImpl.class,
                    "P", PassphraseAccessorImpl.class,
                    "S", SymmetricKeyAccessorImpl.class);

    protected AbstractKeyAccessorImpl(PkiService pkiService) {
        this.pkiService = pkiService;
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
        PASSPHRASE_WRAPPER_TEMP("tempPassphraseWrapperReference")
        ;

        private final String fieldName;

        Fields(String fieldName) {
            this.fieldName = fieldName;
        }

        public String fieldName() {
            return fieldName;
        }
    }

    public void init(KeyAccessorType keyAccessorType, Device device) {
        this.keyAccessorTypeReference.set(keyAccessorType);
        this.deviceReference.set(device);
    }

    public KeyAccessorType getKeyAccessorType() {
        return keyAccessorTypeReference.get();
    }

    public Device getDevice() {
        return deviceReference.get();
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return pkiService.getPropertySpecs(getKeyAccessorType());
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
        if (getActualValue().getProperties().containsValue(null) || getActualValue().getProperties().size()!=getPropertySpecs().size()) {
            return KeyAccessorStatus.INCOMPLETE;
        }
        return KeyAccessorStatus.COMPLETE;
    }
}
