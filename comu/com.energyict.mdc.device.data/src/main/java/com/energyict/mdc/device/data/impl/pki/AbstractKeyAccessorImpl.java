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

import com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.Map;


// TODO validate actual value is present, fix newBlaBla() first to take actual value!
public abstract class AbstractKeyAccessorImpl<T extends SecurityValueWrapper> implements KeyAccessor<T> {
    private final PkiService pkiService;

    private Reference<KeyAccessorType> keyAccessorTypeReference = Reference.empty();
    private Reference<Device> deviceReference = Reference.empty();

    public static final Map<String, Class<? extends KeyAccessor>> IMPLEMENTERS =
            ImmutableMap.of(
                    "C", CertificateAccessorImpl.class,
                    "S", SymmetricKeyAccessorImpl.class);

    protected AbstractKeyAccessorImpl(PkiService pkiService) {
        this.pkiService = pkiService;
    }

    public enum Fields {
        KEY_ACCESSOR_TYPE("keyAccessorTypeReference"),
        DEVICE("deviceReference"),
        CERTIFICATE_WRAPPER_ACTUAL("actualCertificate"),
        CERTIFICATE_WRAPPER_TEMP("tempCertificate"),
        SYMM_KEY_WRAPPER_ACTUAL("actualSymmetricKeyWrapperReference"),
        SYMM_KEY_WRAPPER_TEMP("tempSymmetricKeyWrapperReference")
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

}
