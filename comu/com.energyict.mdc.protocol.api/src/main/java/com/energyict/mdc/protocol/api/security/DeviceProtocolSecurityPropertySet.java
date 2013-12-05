package com.energyict.mdc.protocol.api.security;

import com.energyict.mdc.common.TypedProperties;

/**
 * Maps the SecurityPropertySet to a usable property set for a DeviceProtocol.
 * <p/>
 * Copyrights EnergyICT
 * Date: 21/01/13
 * Time: 15:24
 */
public interface DeviceProtocolSecurityPropertySet {

    /**
     * Gets the configured {@link AuthenticationDeviceAccessLevel}
     *
     * @return the ID of the AuthenticationDeviceAccessLevel
     */
    public int getAuthenticationDeviceAccessLevel();

    /**
     * Gets the configured {@link EncryptionDeviceAccessLevel}
     *
     * @return the ID of the EncryptionDeviceAccessLevel
     */
    public int getEncryptionDeviceAccessLevel();

    /**
     * Gets the used security properties for this Device
     *
     * @return the used security properties
     */
    public TypedProperties getSecurityProperties();

}
