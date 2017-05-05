package com.energyict.mdc.protocol.pluggable.impl.adapters.upl;

import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.mdc.upl.security.AdvancedDeviceProtocolSecurityPropertySet;

/**
 * Maps the securityPropertySet to a usable property set for a DeviceProtocol.
 * <p>
 * <p>
 * Date: 22/01/13
 * Time: 15:05
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
        this.securitySuite = securitySuite;
        this.requestSecurityLevel = requestSecurityLevel;
        this.responseSecurityLevel = responseSecurityLevel;
        this.securityProperties = securityProperties;
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