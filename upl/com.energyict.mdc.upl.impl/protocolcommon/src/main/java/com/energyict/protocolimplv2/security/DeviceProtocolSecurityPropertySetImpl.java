package com.energyict.protocolimplv2.security;

import com.energyict.cpo.TypedProperties;
import com.energyict.mdc.protocol.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.security.SecurityProperty;

import java.util.List;

/**
 * Maps the securityPropertySet to a usable property set for a DeviceProtocol.
 * <p/>
 * Copyrights EnergyICT
 * Date: 22/01/13
 * Time: 15:05
 */
public class DeviceProtocolSecurityPropertySetImpl implements DeviceProtocolSecurityPropertySet {

    private int authenticationDeviceAccessLevel;
    private int encryptionDeviceAccessLevel;
    private TypedProperties securityProperties;

    public DeviceProtocolSecurityPropertySetImpl(List<SecurityProperty> securityProperties) {
        this.securityProperties = TypedProperties.empty();
        for (SecurityProperty securityProperty : securityProperties) {
            this.securityProperties.setProperty(securityProperty.getName(), securityProperty.getValue());
        }
        if (!securityProperties.isEmpty()) {
            authenticationDeviceAccessLevel = securityProperties.get(0).getSecurityPropertySet().getAuthenticationDeviceAccessLevelId();
            encryptionDeviceAccessLevel = securityProperties.get(0).getSecurityPropertySet().getEncryptionDeviceAccessLevelId();
        }
    }

    public DeviceProtocolSecurityPropertySetImpl(int authenticationDeviceAccessLevel, int encryptionDeviceAccessLevel, TypedProperties securityProperties) {
        this.authenticationDeviceAccessLevel = authenticationDeviceAccessLevel;
        this.encryptionDeviceAccessLevel = encryptionDeviceAccessLevel;
        this.securityProperties = securityProperties;
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

}