/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.mdc.services.impl;

import com.elster.jupiter.pki.KeyAccessorType;
import com.elster.jupiter.pki.PkiService;
import com.elster.jupiter.pki.PlaintextPassphrase;
import com.elster.jupiter.pki.PlaintextSymmetricKey;
import com.elster.jupiter.pki.SecurityValueWrapper;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.KeyAccessor;

import javax.inject.Inject;
import javax.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;

/**
 * The KeyAccessorValuePersister can be used to persist a new actual value to
 * a {@link KeyAccessor} of a {@link Device}
 *
 * @author stijn
 * @since 12.05.17 - 11:34
 */
public class KeyAccessorValuePersister {

    private final PkiService pkiService;

    @Inject
    public KeyAccessorValuePersister(PkiService pkiService) {
        this.pkiService = pkiService;
    }

    /**
     * Persists the given key/passphrase as new actual value of the given
     * {@link Device} on the {@link KeyAccessor} of the given type<br/>
     * Note: in case on the {@link Device} no such corresponding {@link KeyAccessor}
     * exists, a new one is created.
     *
     * @param device the {@link Device}
     * @param keyAccessorType the {@link KeyAccessorType}
     * @param newContent the new plaintext symmetric key/passphrase
     * @throws IllegalStateException in case something went wrong
     */
    public void persistKeyAccessorValue(Device device, KeyAccessorType keyAccessorType, String newContent) {
        KeyAccessor<SecurityValueWrapper> keyAccessor = findCorrespondingKeyAccessor(device, keyAccessorType);
        if (!keyAccessor.getActualValue().isPresent()) {
            createNewActualValue(keyAccessor);
        }
        setPropertyOnSecurityAccessor(keyAccessor.getActualValue().get(), newContent);
    }

    private KeyAccessor<SecurityValueWrapper> findCorrespondingKeyAccessor(Device device, KeyAccessorType keyAccessorType) {
        return device.getKeyAccessors()
                .stream()
                .filter(keyAccessor -> keyAccessor.getKeyAccessorType().equals(keyAccessorType))
                .findFirst()
                .orElseGet(() -> device.newKeyAccessor(keyAccessorType));
    }

    private void createNewActualValue(KeyAccessor<SecurityValueWrapper> keyAccessor) {
        SecurityValueWrapper newValue;
        switch (keyAccessor.getKeyAccessorType().getKeyType().getCryptographicType()) {
            case SymmetricKey:
                newValue = pkiService.newSymmetricKeyWrapper(keyAccessor.getKeyAccessorType());
                break;
            case Passphrase:
                newValue = pkiService.newPassphraseWrapper(keyAccessor.getKeyAccessorType());
                break;
            default:
                throw new IllegalStateException("Migration of old security attributes to security accessor type " + keyAccessor.getKeyAccessorType().getName() + " is not supported.");
        }
        keyAccessor.setActualValue(newValue);
        keyAccessor.save();
    }

    private void setPropertyOnSecurityAccessor(SecurityValueWrapper actualValue, String newContent) {
        Map<String, Object> properties = new HashMap<>();
        try {
            if (actualValue instanceof PlaintextSymmetricKey) {
                properties.put("key", newContent);
            } else if (actualValue instanceof PlaintextPassphrase) {
                properties.put("passphrase", newContent);
            } else {
                throw new IllegalStateException("Migration of old security attributes to SecurityValueWrapper " + actualValue.getClass().getSimpleName() + " is not supported.");
            }
            actualValue.setProperties(properties);
        } catch (ConstraintViolationException e) {
            // Most likely because the key size was invalid.
            throw new IllegalStateException("Migration of old security attributes to SecurityValueWrapper " + actualValue.getClass().getSimpleName() + " failed: " + e.getMessage());
        }
    }
}