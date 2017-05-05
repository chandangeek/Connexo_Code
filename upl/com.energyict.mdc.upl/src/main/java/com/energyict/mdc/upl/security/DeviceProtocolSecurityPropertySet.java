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
public interface DeviceProtocolSecurityPropertySet {    //TODO: remove the defaults again and foresee implementation in all protocols!

    /**
     * Gets the
     * @return
     */
    default String getClient() {
        return null;
    }

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
     * Gets the configured {@link SecuritySuite}
     *
     * @return the ID of the {@link SecuritySuite}
     */
    default int getSecuritySuite() {
        return -1;
    }

    /**
     * Gets the configured {@link RequestSecurityLevel}
     *
     * @return the ID of the {@link RequestSecurityLevel}
     */
    default int getRequestSecurityLevel() {
        return -1;
    }

    /**
     * Gets the configured {@link ResponseSecurityLevel}
     *
     * @return the ID of the {@link ResponseSecurityLevel}
     */
    default int getResponseSecurityLevel() {
        return -1;
    }

    /**
     * Gets the used security properties for this Device
     *
     * @return the used security properties
     */
    TypedProperties getSecurityProperties();

}