package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;

/**
 * Maps the securityPropertySet to a usable property set for a DeviceProtocol.
 * <p>
 * Copyrights EnergyICT
 * Date: 22/01/13
 * Time: 15:05
 */
public class DeviceProtocolSecurityPropertySetImpl implements DeviceProtocolSecurityPropertySet {

    private int authenticationDeviceAccessLevel;
    private int encryptionDeviceAccessLevel;
    private TypedProperties securityProperties;

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