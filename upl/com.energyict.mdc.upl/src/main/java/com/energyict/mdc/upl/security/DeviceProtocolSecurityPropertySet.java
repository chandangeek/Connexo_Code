package com.energyict.mdc.upl.security;

import com.energyict.mdc.upl.properties.TypedProperties;

/**
 * Models named set of security properties provided by a {@link DeviceProtocolSecurityCapabilities}.
 * The exact set of {@link com.energyict.mdc.upl.properties.PropertySpec}s
 * that are used is determined by the {@link AuthenticationDeviceAccessLevel}
 * and/or {@link EncryptionDeviceAccessLevel} select in the SecurityPropertySet.
 * That in turn depends on the actual {@link DeviceProtocolSecurityCapabilities}.
 * <p/>
 *
 * Date: 21/01/13
 * Time: 15:24
 */
public interface DeviceProtocolSecurityPropertySet {

    /**
     * Gets the name of the {@link SecurityPropertySet}
     *
     * @return the name
     */
    String getName();

    /**
     * Gets the configured 'client' or null in case the {@link com.energyict.mdc.upl.DeviceProtocol}
     * doesn't support client.
     *
     * @return the client or null
     */
    Object getClient();

    /**
     * Gets the configured {@link AuthenticationDeviceAccessLevel}
     *
     * @return the ID of the {@link AuthenticationDeviceAccessLevel}
     */
    int getAuthenticationDeviceAccessLevel();

    /**
     * Gets the configured {@link EncryptionDeviceAccessLevel}
     *
     * @return the ID of the {@link EncryptionDeviceAccessLevel}
     */
    int getEncryptionDeviceAccessLevel();

    /**
     * Gets the used security properties for this Device
     *
     * @return the used security properties
     */
    TypedProperties getSecurityProperties();

}