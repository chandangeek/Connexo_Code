/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.commands.devices;

import com.elster.jupiter.pki.PlaintextPassphrase;
import com.elster.jupiter.pki.PlaintextSymmetricKey;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.SecurityValueWrapper;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.SecurityAccessor;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

/**
 * The KeyAccessorValuePersister can be used to persist a new actual value to
 * a {@link SecurityAccessor} of a {@link Device}
 *
 * @author stijn
 * @since 12.05.17 - 11:34
 */
public class KeyAccessorValuePersister {

    private final SecurityManagementService securityManagementService;

    @Inject
    public KeyAccessorValuePersister(SecurityManagementService securityManagementService) {
        this.securityManagementService = securityManagementService;
    }

    /**
     * Persists the given key/passphrase as new actual value of the given
     * {@link Device} on the {@link SecurityAccessor} with the given name<br/>
     * Note: in case on the {@link Device} no such corresponding {@link SecurityAccessor}
     * exists, a new one is created.
     *
     * @param device the {@link Device}
     * @param keyAccessorTypeName the name of the {@link SecurityAccessorType}
     * @param newContent the new plaintext symmetric key/passphrase
     */
    public void persistKeyAccessorValue(Device device, String keyAccessorTypeName, String newContent) {
        SecurityAccessorType securityAccessorType = findCorrespondingKeyAccessorType(device.getDeviceConfiguration().getDeviceType(), keyAccessorTypeName);
        SecurityAccessor<SecurityValueWrapper> securityAccessor = findCorrespondingKeyAccessor(device, securityAccessorType);
        if (!securityAccessor.getActualValue().isPresent()) {
            createNewActualValue(securityAccessor);
        }
        setPropertyOnSecurityAccessor(securityAccessor.getActualValue().get(), newContent);
    }

    private SecurityAccessorType findCorrespondingKeyAccessorType(DeviceType deviceType, String keyAccessorTypeName) {
        return deviceType.getSecurityAccessorTypes()
                .stream()
                .filter(keyAccessorType -> keyAccessorType.getName().equals(keyAccessorTypeName))
                .findFirst()
                .orElseThrow(IllegalStateException::new);
    }

    private SecurityAccessor<SecurityValueWrapper> findCorrespondingKeyAccessor(Device device, SecurityAccessorType securityAccessorType) {
        return device.getSecurityAccessors()
                .stream()
                .filter(keyAccessor -> keyAccessor.getSecurityAccessorType().equals(securityAccessorType))
                .findFirst()
                .orElseGet(() -> device.newSecurityAccessor(securityAccessorType));
    }

    private void createNewActualValue(SecurityAccessor<SecurityValueWrapper> securityAccessor) {
        SecurityValueWrapper newValue;
        switch (securityAccessor.getSecurityAccessorType().getKeyType().getCryptographicType()) {
            case SymmetricKey:
                newValue = securityManagementService.newSymmetricKeyWrapper(securityAccessor.getSecurityAccessorType());
                break;
            case Passphrase:
                newValue = securityManagementService.newPassphraseWrapper(securityAccessor.getSecurityAccessorType());
                break;
            default:
                throw new IllegalStateException("Import of values of this security accessor is not supported: " + securityAccessor
                        .getSecurityAccessorType().getName());
        }
        securityAccessor.setActualValue(newValue);
        securityAccessor.save();
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
        actualValue.setProperties(properties);
    }
}
