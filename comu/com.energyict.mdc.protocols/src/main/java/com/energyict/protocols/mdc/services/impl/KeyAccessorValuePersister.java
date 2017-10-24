/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.mdc.services.impl;

import com.elster.jupiter.pki.KeyAccessorType;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.PlaintextPassphrase;
import com.elster.jupiter.pki.PlaintextSymmetricKey;
import com.elster.jupiter.pki.SecurityValueWrapper;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.KeyAccessor;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

/**
 * The KeyAccessorValuePersister can be used to persist a new actual value to
 * a {@link KeyAccessor} of a {@link Device}
 *
 * @author stijn
 * @since 12.05.17 - 11:34
 */
public class KeyAccessorValuePersister {    //TODO: copy from demo - can we make this a service?

    private final SecurityManagementService securityManagementService;

    @Inject
    public KeyAccessorValuePersister(SecurityManagementService securityManagementService) {
        this.securityManagementService = securityManagementService;
    }

    /**
     * Persists the given key/passphrase as new actual value of the given
     * {@link Device} on the {@link KeyAccessor} with the given name<br/>
     * Note: in case on the {@link Device} no such corresponding {@link KeyAccessor}
     * exists, a new one is created.
     *
     * @param device the {@link Device}
     * @param keyAccessorTypeName the name of the {@link KeyAccessorType}
     * @param newContent the new plaintext symmetric key/passphrase
     */
    public void persistKeyAccessorValue(Device device, String keyAccessorTypeName, String newContent) {
        KeyAccessorType keyAccessorType = findCorrespondingKeyAccessorType(device, keyAccessorTypeName);
        KeyAccessor<SecurityValueWrapper> keyAccessor = findCorrespondingKeyAccessor(device, keyAccessorType);
        if (!keyAccessor.getActualValue().isPresent()) {
            createNewActualValue(keyAccessor);
        }
        setPropertyOnSecurityAccessor(keyAccessor.getActualValue().get(), newContent);
    }

    private KeyAccessorType findCorrespondingKeyAccessorType(Device device, String keyAccessorTypeName) {
        DeviceType deviceType = device.getDeviceConfiguration().getDeviceType();
        return deviceType.getKeyAccessorTypes()
                .stream()
                .filter(keyAccessorType -> keyAccessorType.getName().equals(keyAccessorTypeName))
                .findFirst()
                .orElseThrow(IllegalStateException::new);
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
                newValue = securityManagementService.newSymmetricKeyWrapper(keyAccessor.getKeyAccessorType());
                break;
            case Passphrase:
                newValue = securityManagementService.newPassphraseWrapper(keyAccessor.getKeyAccessorType());
                break;
            default:
                throw new IllegalStateException("Import of values of this security accessor is not supported: " + keyAccessor.getKeyAccessorType().getName());
        }
        keyAccessor.setActualValue(newValue);
        keyAccessor.save();
    }

    private void setPropertyOnSecurityAccessor(SecurityValueWrapper actualValue, String newContent) {
        Map<String, Object> properties = new HashMap<>();
        if (actualValue instanceof PlaintextSymmetricKey) {
            properties.put("key", newContent);
        } else if (actualValue instanceof PlaintextPassphrase) {
            properties.put("passphrase", newContent);
        } else {
            throw new IllegalStateException("");
        }
        actualValue.setProperties(properties);      //TODO: in case key has invalid size, then you probably will end up in an error
    }
}