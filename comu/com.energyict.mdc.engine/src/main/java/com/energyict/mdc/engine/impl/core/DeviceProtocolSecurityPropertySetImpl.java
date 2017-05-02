/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.PlaintextPassphrase;
import com.elster.jupiter.pki.PlaintextSymmetricKey;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.ConfigurationSecurityProperty;
import com.energyict.mdc.device.data.KeyAccessor;
import com.energyict.mdc.engine.impl.commands.offline.OfflineKeyAccessorImpl;
import com.energyict.mdc.protocol.api.device.offline.OfflineKeyAccessor;
import com.energyict.mdc.protocol.api.security.AdvancedDeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.api.services.IdentificationService;

import java.util.List;
import java.util.Optional;

/**
 * Maps the securityPropertySet to a usable property set for a DeviceProtocol.
 */
public class DeviceProtocolSecurityPropertySetImpl implements AdvancedDeviceProtocolSecurityPropertySet {

    private final String client;
    private final int securitySuite;
    private final int requestSecurityLevel;
    private final int responseSecurityLevel;
    private final int authenticationDeviceAccessLevel;
    private final int encryptionDeviceAccessLevel;
    private final TypedProperties securityProperties = TypedProperties.empty();

    public DeviceProtocolSecurityPropertySetImpl(String client, int authenticationDeviceAccessLevel, int encryptionDeviceAccessLevel, int securitySuite, int requestSecurityLevel, int responseSecurityLevel, List<ConfigurationSecurityProperty> configurationSecurityProperties, List<KeyAccessor> keyAccessors, IdentificationService identificationService) {
        this.client = client;
        this.authenticationDeviceAccessLevel = authenticationDeviceAccessLevel;
        this.encryptionDeviceAccessLevel = encryptionDeviceAccessLevel;
        this.securitySuite = securitySuite;
        this.requestSecurityLevel = requestSecurityLevel;
        this.responseSecurityLevel = responseSecurityLevel;
        this.constructSecurityProperties(configurationSecurityProperties, keyAccessors, identificationService);

    }

    private void constructSecurityProperties(List<ConfigurationSecurityProperty> configurationSecurityProperties, List<KeyAccessor> keyAccessors, IdentificationService identificationService) {
        for (ConfigurationSecurityProperty configurationSecurityProperty : configurationSecurityProperties) {
            Optional<OfflineKeyAccessor> offlineKeyAccessor = keyAccessors
                    .stream()
                    .filter(keyAccessor -> keyAccessor.getKeyAccessorType().getName().equals(configurationSecurityProperty.getKeyAccessorType().getName()))
                    .findFirst()
                    .map(keyAccessor -> new OfflineKeyAccessorImpl(keyAccessor, identificationService));
            if (offlineKeyAccessor.isPresent()) {
                if (offlineKeyAccessor.get().getActualValue() instanceof PlaintextSymmetricKey) {
                    PlaintextSymmetricKey plaintextSymmetricKey = (PlaintextSymmetricKey) offlineKeyAccessor.get().getActualValue();
                    if (plaintextSymmetricKey.getKey().isPresent()) {
                        securityProperties.setProperty(configurationSecurityProperty.getName(), plaintextSymmetricKey.getKey().get().getEncoded());
                    }
                } else if (offlineKeyAccessor.get().getActualValue() instanceof PlaintextPassphrase) {
                    PlaintextPassphrase plaintextPassphrase = (PlaintextPassphrase) offlineKeyAccessor.get().getActualValue();
                    if (plaintextPassphrase.getPassphrase().isPresent()) {
                        securityProperties.setProperty(configurationSecurityProperty.getName(), plaintextPassphrase.getPassphrase().get());
                    }
                } else if (offlineKeyAccessor.get().getActualValue() instanceof CertificateWrapper) {
                    //TODO: foresee offline variants for CertificateWrapper
                }
            }
            // TODO: in case optional is not present, should we silently ignore/or throw an error instead?
        }
    }

    public String getClient() {
        return client;
    }

    @Override
    public int getAuthenticationDeviceAccessLevel() {
        return authenticationDeviceAccessLevel;
    }

    @Override
    public int getEncryptionDeviceAccessLevel() {
        return encryptionDeviceAccessLevel;
    }

    @Override
    public int getSecuritySuite() {
        return securitySuite;
    }

    @Override
    public int getRequestSecurityLevel() {
        return requestSecurityLevel;
    }

    @Override
    public int getResponseSecurityLevel() {
        return responseSecurityLevel;
    }

    @Override
    public TypedProperties getSecurityProperties() {
        return securityProperties;
    }
}