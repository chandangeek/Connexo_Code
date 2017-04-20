/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.protocol.api.security.AdvancedDeviceProtocolSecurityPropertySet;

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
    private final TypedProperties securityProperties;

    public DeviceProtocolSecurityPropertySetImpl(String client, int authenticationDeviceAccessLevel, int encryptionDeviceAccessLevel, int securitySuite, int requestSecurityLevel, int responseSecurityLevel, TypedProperties securityProperties) {
        this.client = client;
        this.authenticationDeviceAccessLevel = authenticationDeviceAccessLevel;
        this.encryptionDeviceAccessLevel = encryptionDeviceAccessLevel;
        this.securityProperties = securityProperties;
        this.securitySuite = securitySuite;
        this.requestSecurityLevel = requestSecurityLevel;
        this.responseSecurityLevel = responseSecurityLevel;
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
    public TypedProperties getSecurityProperties() {
        return securityProperties;
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
}