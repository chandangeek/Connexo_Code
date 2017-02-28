package com.energyict.protocolimplv2.security;

import com.energyict.mdc.protocol.security.AdvancedDeviceProtocolSecurityPropertySet;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.mdc.upl.security.SecurityProperty;

import java.util.List;

/**
 * Maps the securityPropertySet to a usable property set for a DeviceProtocol.
 * <p/>
 * Copyrights EnergyICT
 * Date: 22/01/13
 * Time: 15:05
 */
public class DeviceProtocolSecurityPropertySetImpl implements AdvancedDeviceProtocolSecurityPropertySet {

    private int authenticationDeviceAccessLevel;
    private int encryptionDeviceAccessLevel;
    private int securitySuite;
    private int requestSecurityLevel;
    private int responseSecurityLevel;
    private TypedProperties securityProperties;

    public DeviceProtocolSecurityPropertySetImpl(List<? extends SecurityProperty> securityProperties) {
        this.securityProperties = com.energyict.protocolimpl.properties.TypedProperties.empty();
        for (SecurityProperty securityProperty : securityProperties) {
            this.securityProperties.setProperty(securityProperty.getName(), securityProperty.getValue());
        }
        if (!securityProperties.isEmpty()) {
            SecurityProperty firstProperty = securityProperties.get(0);
            authenticationDeviceAccessLevel = firstProperty.getSecurityPropertySet().getAuthenticationDeviceAccessLevelId();
            encryptionDeviceAccessLevel = firstProperty.getSecurityPropertySet().getEncryptionDeviceAccessLevelId();
            securitySuite = firstProperty.getSecurityPropertySet().getSecuritySuiteId();
            requestSecurityLevel = firstProperty.getSecurityPropertySet().getRequestSecurityLevelId();
            responseSecurityLevel = firstProperty.getSecurityPropertySet().getResponseSecurityLevelId();
        }
    }

    public DeviceProtocolSecurityPropertySetImpl(int authenticationDeviceAccessLevel, int encryptionDeviceAccessLevel, int securitySuite, int requestSecurityLevel, int responseSecurityLevel, TypedProperties securityProperties) {
        this.authenticationDeviceAccessLevel = authenticationDeviceAccessLevel;
        this.encryptionDeviceAccessLevel = encryptionDeviceAccessLevel;
        this.securityProperties = securityProperties;
        this.securitySuite = securitySuite;
        this.requestSecurityLevel = requestSecurityLevel;
        this.responseSecurityLevel = responseSecurityLevel;
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