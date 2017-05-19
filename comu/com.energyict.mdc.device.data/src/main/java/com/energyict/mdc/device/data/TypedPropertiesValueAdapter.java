/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data;

import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.KeyAccessorType;
import com.elster.jupiter.pki.PlaintextPassphrase;
import com.elster.jupiter.pki.PlaintextSymmetricKey;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineKeyAccessor;

import javax.xml.bind.DatatypeConverter;
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

    /**
     * Adapts the CXO values (given as {@link TypedProperties} to UPL values, so that the 9.1 protocols can use them.<br/>
     * <b>Note that:</b> Values of type KeyAccessorType will be resolved to actual KeyAccessor value using {@link Device#getKeyAccessors()}.
     *
     * @param device the {@link Device} for which the CXO values should be adapted
     * @param typedProperties the CXO values to adapt
     * @return the UPL values usable by 9.1 protocols
     */
    public static com.energyict.mdc.upl.properties.TypedProperties adaptToUPLValues(Device device, TypedProperties typedProperties) {
        TypedProperties result = TypedProperties.copyOf(typedProperties);
        for (String name : typedProperties.propertyNames()) {
            Object propertyValue = typedProperties.getTypedProperty(name);
            if (propertyValue instanceof KeyAccessorType) {
                result.setProperty(name, adaptToUPLValue(device, propertyValue));
            }
        }
        return com.energyict.mdc.protocol.pluggable.adapters.upl.TypedPropertiesValueAdapter.adaptToUPLValues(result);
    }

    /**
     * Adapts the given CXO value to UPL value, so that the 9.1 protocols can use it.<br/>
     * <b>Note that:</b> Value of type KeyAccessorType will be resolved to actual KeyAccessor value using {@link Device#getKeyAccessors()}.
     *
     * @param device the {@link Device} for which the CXO values should be adapted
     * @param value the CXO value to adapt
     * @return the UPL value usable by 9.1 protocols
     */
    public static Object adaptToUPLValue(Device device, Object value) {
        return (value instanceof KeyAccessorType)
                ? adaptKeyAccessorTypeToUPLValue(device, (KeyAccessorType) value)
                : com.energyict.mdc.protocol.pluggable.adapters.upl.TypedPropertiesValueAdapter.adaptToUPLValue(value);
    }

    /**
     * Adapts the CXO values (given as {@link TypedProperties} to UPL values, so that the 9.1 protocols can use them.<br/>
     * <b>Note that:</b> Values of type KeyAccessorType will be resolved to actual KeyAccessor value using {@link OfflineDevice#getAllOfflineKeyAccessors}.
     *
     * @param device the {@link Device} for which the CXO values should be adapted
     * @param typedProperties the CXO values to adapt
     * @return the UPL values usable by 9.1 protocols
     */
    public static com.energyict.mdc.upl.properties.TypedProperties adaptToUPLValues(OfflineDevice device, TypedProperties typedProperties) {
        TypedProperties result = TypedProperties.copyOf(typedProperties);
        for (String name : typedProperties.propertyNames()) {
            Object propertyValue = typedProperties.getTypedProperty(name);
            if (propertyValue instanceof KeyAccessorType) {
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
     * @param value the CXO value to adapt
     * @return the UPL value usable by 9.1 protocols
     */
    public static Object adaptToUPLValue(OfflineDevice device, Object value) {
        return (value instanceof KeyAccessorType)
                ? adaptKeyAccessorTypeToUPLValue(device, (KeyAccessorType) value)
                : com.energyict.mdc.protocol.pluggable.adapters.upl.TypedPropertiesValueAdapter.adaptToUPLValue(value);
    }

    private static Object adaptKeyAccessorTypeToUPLValue(Device device, KeyAccessorType keyAccessorType) {
        Optional<KeyAccessor> optionalKeyAccessor = device.getKeyAccessors()
                .stream()
                .filter(keyAccessor -> keyAccessor.getKeyAccessorType().getId() == keyAccessorType.getId())
                .findFirst();

        if (optionalKeyAccessor.isPresent() && optionalKeyAccessor.get().getActualValue().isPresent()) {
            Object actualValue = optionalKeyAccessor.get().getActualValue().get();
            return adaptActualValueToUPLValue(actualValue);
        }
        return null; // Return value as-is
    }

    private static Object adaptKeyAccessorTypeToUPLValue(OfflineDevice device, KeyAccessorType keyAccessorType) {
        Optional<OfflineKeyAccessor> optionalKeyAccessor = device.getAllOfflineKeyAccessors()
                .stream()
                .filter(keyAccessor -> keyAccessor.getKeyAccessorType().getId() == keyAccessorType.getId())
                .findFirst();

        if (optionalKeyAccessor.isPresent() && optionalKeyAccessor.get().getActualValue().isPresent()) {
            Object actualValue = optionalKeyAccessor.get().getActualValue().get();
            return adaptActualValueToUPLValue(actualValue);
        }
        return null;
    }

    private static Object adaptActualValueToUPLValue(Object actualValue) {
        if (actualValue instanceof PlaintextSymmetricKey) {
            PlaintextSymmetricKey plaintextSymmetricKey = (PlaintextSymmetricKey) actualValue;
            if (plaintextSymmetricKey.getKey().isPresent()) {
                return DatatypeConverter.printHexBinary(plaintextSymmetricKey.getKey().get().getEncoded());
            }
        } else if (actualValue instanceof PlaintextPassphrase) {
            PlaintextPassphrase plaintextPassphrase = (PlaintextPassphrase) actualValue;
            if (plaintextPassphrase.getPassphrase().isPresent()) {
                return plaintextPassphrase.getPassphrase().get();
            }
        } else if (actualValue instanceof CertificateWrapper) {
            CertificateWrapper certificateWrapper = (CertificateWrapper) actualValue;
            return certificateWrapper;
        }
        return null;
    }

    // Hide utility class constructor
    private TypedPropertiesValueAdapter() {
    }
}