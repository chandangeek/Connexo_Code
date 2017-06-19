package com.energyict.protocolimplv2.security;

import com.energyict.mdc.protocol.security.AdvancedDeviceProtocolSecurityPropertySet;
import com.energyict.mdc.upl.DeviceMasterDataExtractor;
import com.energyict.mdc.upl.properties.TypedProperties;

import java.util.List;

/**
 * Maps the securityPropertySet to a usable property set for a DeviceProtocol.
 * <p/>
 * Copyrights EnergyICT
 * Date: 22/01/13
 * Time: 15:05
 */
public class DeviceProtocolSecurityPropertySetImpl implements AdvancedDeviceProtocolSecurityPropertySet {

    private String name;
    private Object client;
    private int authenticationDeviceAccessLevel;
    private int encryptionDeviceAccessLevel;
    private int securitySuite;
    private int requestSecurityLevel;
    private int responseSecurityLevel;
    private TypedProperties securityProperties;

    public DeviceProtocolSecurityPropertySetImpl(DeviceMasterDataExtractor.SecurityPropertySet securityPropertySet, List<? extends DeviceMasterDataExtractor.SecurityProperty> securityProperties) {
        this.securityProperties = com.energyict.mdc.upl.TypedProperties.empty();
        for (DeviceMasterDataExtractor.SecurityProperty securityProperty : securityProperties) {
            this.securityProperties.setProperty(securityProperty.name(), securityProperty.value());
        }

        if (securityPropertySet != null){
            name = securityPropertySet.name();
            client = securityPropertySet.client();
            authenticationDeviceAccessLevel = securityPropertySet.authenticationDeviceAccessLevelId();
            encryptionDeviceAccessLevel = securityPropertySet.encryptionDeviceAccessLevelId();
            securitySuite = securityPropertySet.securitySuite();
            requestSecurityLevel = securityPropertySet.requestSecurityLevelId();
            responseSecurityLevel = securityPropertySet.responseSecurityLevelId();
        }
    }

    public DeviceProtocolSecurityPropertySetImpl(Object client, int authenticationDeviceAccessLevel, int encryptionDeviceAccessLevel, int securitySuite, int requestSecurityLevel, int responseSecurityLevel, TypedProperties securityProperties) {
        this.name = "unknown";
        this.client = client;
        this.authenticationDeviceAccessLevel = authenticationDeviceAccessLevel;
        this.encryptionDeviceAccessLevel = encryptionDeviceAccessLevel;
        this.securityProperties = securityProperties;
        this.securitySuite = securitySuite;
        this.requestSecurityLevel = requestSecurityLevel;
        this.responseSecurityLevel = responseSecurityLevel;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
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