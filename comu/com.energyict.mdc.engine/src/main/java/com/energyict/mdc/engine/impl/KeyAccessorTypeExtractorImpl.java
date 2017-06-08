/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl;

import com.elster.jupiter.pki.PlaintextPassphrase;
import com.elster.jupiter.pki.PlaintextSymmetricKey;
import com.energyict.mdc.protocol.api.device.offline.OfflineKeyAccessor;
import com.energyict.mdc.protocol.pluggable.adapters.upl.KeyAccessorTypeAdapter;
import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.security.CertificateWrapper;
import com.energyict.mdc.upl.security.KeyAccessorType;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import javax.xml.bind.DatatypeConverter;
import java.util.Optional;

/**
 * Provides an implementation for the {@link KeyAccessorTypeExtractor} interface
 *
 * @author stijn
 * @since 11.05.17 - 08:46
 */
@Component(name = "com.energyict.mdc.upl.key.accessor.type.extractor", service = {KeyAccessorTypeExtractor.class}, immediate = true)
public class KeyAccessorTypeExtractorImpl implements KeyAccessorTypeExtractor {

    private ThreadLocal<ThreadContextImpl> threadContextThreadLocal = ThreadLocal.withInitial(ThreadContextImpl::new);

    @Activate
    public void activate() {
        Services.keyAccessorTypeExtractor(this);
    }

    @Deactivate
    public void deactivate() {
        Services.keyAccessorTypeExtractor(null);
    }

    @Override
    public ThreadContextImpl threadContext() {
        return this.threadContextThreadLocal.get();
    }

    private com.elster.jupiter.pki.KeyAccessorType toConnexoKeyAccessorType(KeyAccessorType keyAccessorType) {
        return ((KeyAccessorTypeAdapter) keyAccessorType).getKeyAccessorType();
    }

    private com.energyict.mdc.protocol.api.device.offline.OfflineDevice toConnexoDevice(OfflineDevice device) {
        return ((com.energyict.mdc.protocol.api.device.offline.OfflineDevice) device);
    }

    @Override
    public String id(KeyAccessorType keyAccessorType) {
        return Long.toString(this.toConnexoKeyAccessorType(keyAccessorType).getId());
    }

    @Override
    public String name(KeyAccessorType keyAccessorType) {
        return this.toConnexoKeyAccessorType(keyAccessorType).getName();
    }

    @Override
    public Optional<Object> actualValue(KeyAccessorType keyAccessorType) {
        com.elster.jupiter.pki.KeyAccessorType connexoKeyAccessorType = this.toConnexoKeyAccessorType(keyAccessorType);
        Optional<OfflineKeyAccessor> offlineKeyAccessor = toConnexoDevice(threadContext().getDevice()).getAllOfflineKeyAccessors()
                .stream()
                .filter(keyAccessor -> keyAccessor.getKeyAccessorType().getName().equals(connexoKeyAccessorType.getName()))
                .findFirst();
        return (offlineKeyAccessor.isPresent() && offlineKeyAccessor.get().getActualValue().isPresent())
                ? extractUplValueOutOf(offlineKeyAccessor, offlineKeyAccessor.get().getActualValue().get())
                : Optional.empty();
    }

    @Override
    public String actualValueContent(KeyAccessorType keyAccessorType) {
        Optional<Object> optional = actualValue(keyAccessorType);
        return convertSecurityValueToString(optional);
    }

    @Override
    public Optional<Object> passiveValue(KeyAccessorType keyAccessorType) {
        com.elster.jupiter.pki.KeyAccessorType connexoKeyAccessorType = this.toConnexoKeyAccessorType(keyAccessorType);
        Optional<OfflineKeyAccessor> offlineKeyAccessor = toConnexoDevice(threadContext().getDevice()).getAllOfflineKeyAccessors()
                .stream()
                .filter(keyAccessor -> keyAccessor.getKeyAccessorType().equals(connexoKeyAccessorType))
                .findFirst();
        return (offlineKeyAccessor.isPresent() && offlineKeyAccessor.get().getTempValue().isPresent())
                ? extractUplValueOutOf(offlineKeyAccessor, offlineKeyAccessor.get().getTempValue().get())
                : Optional.empty();
    }

    @Override
    public String passiveValueContent(KeyAccessorType keyAccessorType) {
        Optional<Object> optional = passiveValue(keyAccessorType);
        return convertSecurityValueToString(optional);
    }

    private String convertSecurityValueToString(Optional<Object> optional) {
        if (optional.isPresent() && optional.get() instanceof CertificateWrapper) {
            throw new UnsupportedOperationException("Support to format CertificateWrapper as text does not exist.");    //TODO: once needed, support should be added off-course
        }
        return optional.isPresent() ? optional.get().toString() : ""; // Note that the 'toString() should work fine for symmetric keys/passwords
    }

    private Optional<Object> extractUplValueOutOf(Optional<OfflineKeyAccessor> offlineKeyAccessor, Object value) {
        if (value instanceof PlaintextSymmetricKey) {
            return handlePlainTextSymmetricKey((PlaintextSymmetricKey) value);
        } else if (value instanceof PlaintextPassphrase) {
            return handlePlainTextPassphrase((PlaintextPassphrase) value);
        } else if (value instanceof CertificateWrapper) {
            return Optional.of(value);  //Return instance of CertificateWrapper as-is
        }
        return null;
    }

    private Optional<Object> handlePlainTextSymmetricKey(PlaintextSymmetricKey plaintextSymmetricKey) {
        if (plaintextSymmetricKey.getKey().isPresent()) {
            return Optional.of(DatatypeConverter.printHexBinary(plaintextSymmetricKey.getKey().get().getEncoded()));
        }
        return Optional.empty();
    }

    private Optional<Object> handlePlainTextPassphrase(PlaintextPassphrase plaintextPassphrase) {
        if (plaintextPassphrase.getPassphrase().isPresent()) {
            return Optional.of(plaintextPassphrase.getPassphrase().get());
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> correspondingSecurityAttribute(String keyAccessorType, String securityPropertySetName) {
        TypedProperties properties = toConnexoDevice(threadContext().getDevice()).getSecurityPropertySetAttributeToKeyAccessorTypeMapping().get(securityPropertySetName);
        return properties.propertyNames()
                .stream()
                .filter(propertyName -> properties.getProperty(propertyName).equals(keyAccessorType))
                .findFirst();
    }

    private static class ThreadContextImpl implements ThreadContext {
        private OfflineDevice device;

        @Override
        public OfflineDevice getDevice() {
            return device;
        }

        @Override
        public void setDevice(OfflineDevice device) {
            this.device = device;
        }
    }
}