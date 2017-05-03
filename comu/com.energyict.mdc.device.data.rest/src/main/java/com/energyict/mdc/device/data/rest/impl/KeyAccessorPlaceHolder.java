/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.pki.CryptographicType;
import com.elster.jupiter.pki.KeyAccessorType;
import com.elster.jupiter.pki.PkiService;
import com.elster.jupiter.pki.SecurityValueWrapper;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.KeyAccessor;
import com.energyict.mdc.device.data.KeyAccessorStatus;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This placeholder is a runtime-mock of KeyAccessors. Whenever the KeyAccessors for a device are retrieved from domain (Device::getKeyAccessors()),
 * only those KeyAccessors that actually exist are listed (surprised?), however, front end wants to display property specs even for the none-existing
 * key accessors, that is, KeyAccessorType (on device type) that do not (yet) have a KeyAccessor on the device. That's where this object comes in: it serves
 * as place holder for the to-be-created KeyAccessors. This holder returns just enough info for MdcPropertyUtils and FrontEnd to do their job.
 * So REST does not use Device::getKeyAccessors() but instead builds its own list using Device::getKeyAccessor(KAT)
 */
public class KeyAccessorPlaceHolder implements KeyAccessor {
    private final PkiService pkiService;

    private KeyAccessorType kat;
    private Device device;

    @Inject
    public KeyAccessorPlaceHolder(PkiService pkiService) {
        this.pkiService = pkiService;
    }

    public KeyAccessor init(KeyAccessorType kat, Device device) {
        this.kat = kat;
        this.device = device;
        return this;
    }

    @Override
    public Device getDevice() {
        return device;
    }

    @Override
    public KeyAccessorType getKeyAccessorType() {
        return kat;
    }

    @Override
    public Optional getActualValue() {
        return Optional.of(new SecurityValueWrapper() {
            @Override
            public Optional<Instant> getExpirationTime() {
                return Optional.empty();
            }

            @Override
            public void setProperties(Map<String, Object> properties) {

            }

            @Override
            public Map<String, Object> getProperties() {
                Map<String, Object> properties = new HashMap<>();
                if (getKeyAccessorType().getKeyType().getCryptographicType().equals(CryptographicType.TrustedCertificate)) {
                    getKeyAccessorType().getTrustStore().ifPresent(ts -> properties.put("trustStore", ts));
                }
                return properties;
            }

            @Override
            public List<PropertySpec> getPropertySpecs() {
                return Collections.emptyList();
            }
        });
    }

    @Override
    public void setActualValue(SecurityValueWrapper newWrapperValue) {

    }

    @Override
    public Optional getTempValue() {
        return Optional.empty();
    }

    @Override
    public void setTempValue(SecurityValueWrapper newValueWrapper) {

    }

    @Override
    public void renew() {

    }

    @Override
    public void swapValues() {

    }

    @Override
    public void clearTempValue() {

    }

    @Override
    public void clearActualValue() {

    }

    @Override
    public void save() {

    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return pkiService.getPropertySpecs(kat);
    }

    @Override
    public void delete() {
        
    }

    @Override
    public long getVersion() {
        return -1;
    }

    @Override
    public boolean isSwapped() {
        return false;
    }

    @Override
    public Instant getModTime() {
        return null;
    }

    @Override
    public KeyAccessorStatus getStatus() {
        return KeyAccessorStatus.INCOMPLETE;
    }
}
