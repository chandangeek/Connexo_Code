/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.protocol.security.AdvancedDeviceProtocolSecurityPropertySet;
import com.energyict.mdc.upl.TypedProperties;

/**
 * Maps the securityPropertySet to a usable property set for a DeviceProtocol.
 */
public class DeviceProtocolSecurityPropertySetImpl implements AdvancedDeviceProtocolSecurityPropertySet {

    private final String name;
    private final Object client;
    private final int securitySuite;
    private final int requestSecurityLevel;
    private final int responseSecurityLevel;
    private final int authenticationDeviceAccessLevel;
    private final int encryptionDeviceAccessLevel;
    private final TypedProperties securityProperties;

    public DeviceProtocolSecurityPropertySetImpl(String name, Object client, int authenticationDeviceAccessLevel, int encryptionDeviceAccessLevel, int securitySuite, int requestSecurityLevel, int responseSecurityLevel, TypedProperties securityProperties) {
        this.name = name;
        this.client = client;
        this.authenticationDeviceAccessLevel = authenticationDeviceAccessLevel;
        this.encryptionDeviceAccessLevel = encryptionDeviceAccessLevel;
        this.securitySuite = securitySuite;
        this.requestSecurityLevel = requestSecurityLevel;
        this.responseSecurityLevel = responseSecurityLevel;
        this.securityProperties = securityProperties;
    }

    @Override
    public String getName() {
        return name;
    }

    public Object getClient() {
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