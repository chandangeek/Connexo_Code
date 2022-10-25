/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data;

import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.HsmKey;
import com.elster.jupiter.pki.PlaintextPassphrase;
import com.elster.jupiter.pki.PlaintextSymmetricKey;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.TrustedCertificate;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.SecurityAccessor;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineKeyAccessor;
import com.energyict.mdc.protocol.pluggable.adapters.upl.CertificateWrapperAdapter;
import com.energyict.mdc.upl.TypedProperties;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Optional;

/**
 * Adapts the CXO values of the given TypedProperties to UPL values, so that the 9.1 protocols can use them.<br/>
 * Values of type KeyAccessorType will be converted to actual symmetric key/passphrase/certificate value,
 * which is taken from the corresponding KeyAccessor of the given Device and formatted so it becomes usage for the 9.1 protocols.
 * (e.g.: a KeyAccessorType for a symmetric key will be adapted to a String containing the actual key as HEX) *
 *
 * @author Stijn Vanhoorelbeke
 * @since 16/05/2017 - 13:49
 */
public class TypedPropertiesValueAdapter {

    private static final String TRUST_STORE = "JCEKS";

    /**
     * Adapts the CXO values (given as {@link TypedProperties} to UPL values, so that the 9.1 protocols can use them.<br/>
     * <b>Note that:</b> Values of type KeyAccessorType will be resolved to actual KeyAccessor value using {@link Device#getSecurityAccessors()}.
     *
     * @param device          the {@link Device} for which the CXO values should be adapted
     * @param typedProperties the CXO values to adapt
     * @return the UPL values usable by 9.1 protocols
     */
    public static com.energyict.mdc.upl.properties.TypedProperties adaptToUPLValues(Device device, TypedProperties typedProperties) {
        TypedProperties result = TypedProperties.copyOf(typedProperties);
        for (String name : typedProperties.propertyNames()) {
            Object propertyValue = typedProperties.getTypedProperty(name);
            if (propertyValue instanceof SecurityAccessorType) {
                result.setProperty(name, adaptToUPLValue(device, propertyValue));
            }
        }
        return com.energyict.mdc.protocol.pluggable.adapters.upl.TypedPropertiesValueAdapter.adaptToUPLValues(result);
    }

    /**
     * Adapts the given CXO value to UPL value, so that the 9.1 protocols can use it.<br/>
     * <b>Note that:</b> Value of type KeyAccessorType will be resolved to actual KeyAccessor value using {@link Device#getSecurityAccessors()}.
     *
     * @param device the {@link Device} for which the CXO values should be adapted
     * @param value  the CXO value to adapt
     * @return the UPL value usable by 9.1 protocols
     */
    public static Object adaptToUPLValue(Device device, Object value) {
        return (value instanceof SecurityAccessorType)
                ? adaptKeyAccessorTypeToUPLValue(device, (SecurityAccessorType) value)
                : com.energyict.mdc.protocol.pluggable.adapters.upl.TypedPropertiesValueAdapter.adaptToUPLValue(value);
    }

    /**
     * Adapts the CXO values (given as {@link TypedProperties} to UPL values, so that the 9.1 protocols can use them.<br/>
     * <b>Note that:</b> Values of type KeyAccessorType will be resolved to actual KeyAccessor value using {@link OfflineDevice#getAllOfflineKeyAccessors}.
     *
     * @param device          the {@link Device} for which the CXO values should be adapted
     * @param typedProperties the CXO values to adapt
     * @return the UPL values usable by 9.1 protocols
     */
    public static com.energyict.mdc.upl.properties.TypedProperties adaptToUPLValues(OfflineDevice device, TypedProperties typedProperties) {
        TypedProperties result = TypedProperties.copyOf(typedProperties);
        for (String name : typedProperties.propertyNames()) {
            Object propertyValue = typedProperties.getTypedProperty(name);
            if (propertyValue instanceof SecurityAccessorType) {
                result.setProperty(name, adaptToUPLValue(device, propertyValue));
            }
        }
        return com.energyict.mdc.protocol.pluggable.adapters.upl.TypedPropertiesValueAdapter.adaptToUPLValues(result);
    }

    /**
     * Adapts the given CXO value to UPL value, so that the 9.1 protocols can use it.<br/>
     * <b>Note that:</b> Value of type KeyAccessorType will be resolved to actual KeyAccessor value using {@link OfflineDevice#getAllOfflineKeyAccessors}.
     *
     * @param device the {@link Device} for which the CXO values should be adapted
     * @param value  the CXO value to adapt
     * @return the UPL value usable by 9.1 protocols
     */
    public static Object adaptToUPLValue(OfflineDevice device, Object value) {
        return (value instanceof SecurityAccessorType)
                ? adaptKeyAccessorTypeToUPLValue(device, (SecurityAccessorType) value)
                : com.energyict.mdc.protocol.pluggable.adapters.upl.TypedPropertiesValueAdapter.adaptToUPLValue(value);
    }

    private static Object adaptKeyAccessorTypeToUPLValue(Device device, SecurityAccessorType securityAccessorType) {
        Optional<SecurityAccessor> optionalKeyAccessor = device.getSecurityAccessors()
                .stream()
                .filter(keyAccessor -> keyAccessor.getSecurityAccessorType().getId() == securityAccessorType.getId())
                .findFirst();

        if (optionalKeyAccessor.isPresent() && optionalKeyAccessor.get().getActualValue().isPresent()) {
            Object actualValue = optionalKeyAccessor.get().getActualValue().get();
            return adaptActualValueToUPLValue(actualValue, securityAccessorType);
        }
        return null; // Return value as-is
    }

    private static Object adaptKeyAccessorTypeToUPLValue(OfflineDevice device, SecurityAccessorType securityAccessorType) {
        Optional<OfflineKeyAccessor> optionalKeyAccessor = device.getAllOfflineKeyAccessors()
                .stream()
                .filter(keyAccessor -> keyAccessor.getSecurityAccessorType().getId() == securityAccessorType.getId())
                .findFirst();

        if (optionalKeyAccessor.isPresent() && optionalKeyAccessor.get().getActualValue().isPresent()) {
            Object actualValue = optionalKeyAccessor.get().getActualValue().get();
            return adaptActualValueToUPLValue(actualValue, securityAccessorType);
        }
        return null;
    }

    public static Object adaptActualValueToUPLValue(Object actualValue, SecurityAccessorType securityAccessorType) {
        if (actualValue instanceof PlaintextSymmetricKey) {
            PlaintextSymmetricKey plaintextSymmetricKey = (PlaintextSymmetricKey) actualValue;
            if (plaintextSymmetricKey.getKey().isPresent()) {
                return DatatypeConverter.printHexBinary(plaintextSymmetricKey.getKey().get().getEncoded());
            }
        } else if (actualValue instanceof PlaintextPassphrase) {
            PlaintextPassphrase plaintextPassphrase = (PlaintextPassphrase) actualValue;
            if (plaintextPassphrase.getEncryptedPassphrase().isPresent()) {
                return plaintextPassphrase.getEncryptedPassphrase().get();
            }
        } else if (actualValue instanceof CertificateWrapper) {
            //Also include the trust store of this CertificateWrapper (if it's present)
            Optional<KeyStore> optionalKeyStore = Optional.empty();
            if (securityAccessorType.getTrustStore().isPresent()) {
                try {
                    KeyStore keyStore = KeyStore.getInstance(TRUST_STORE);
                    keyStore.load(null); // This initializes the empty key store
                    for (TrustedCertificate trustedCertificate : securityAccessorType.getTrustStore().get().getCertificates()) {
                        if (trustedCertificate.getCertificate().isPresent()) {
                            keyStore.setCertificateEntry(trustedCertificate.getAlias(), trustedCertificate.getCertificate().get());
                        }
                    }
                    optionalKeyStore = Optional.of(keyStore);
                } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException e) {
                    throw new IllegalStateException(e);
                }
            }

            return new CertificateWrapperAdapter((CertificateWrapper) actualValue, optionalKeyStore);
        } else if(actualValue instanceof HsmKey) {
            HsmKey hsmKey = (HsmKey) actualValue;
            if (hsmKey.getKey().length > 0 && !hsmKey.getLabel().isEmpty()) {
                return hsmKey.getLabel() + ":" + DatatypeConverter.printHexBinary(hsmKey.getKey());
            }
        }
        return null;
    }

    // Hide utility class constructor
    private TypedPropertiesValueAdapter() {
    }
}
