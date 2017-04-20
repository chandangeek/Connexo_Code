/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.security;

import com.energyict.mdc.common.TypedProperties;

public interface DeviceProtocolSecurityPropertySet {

    /**
     * Gets the configured client
     *
     * @return the configured client
     */
    default String getClient() {
        return ""; //TODO: foresee useful implementation on the protocols who need this
    }

    /**
     * Gets the configured {@link AuthenticationDeviceAccessLevel}
     *
     * @return the ID of the AuthenticationDeviceAccessLevel
     */
    int getAuthenticationDeviceAccessLevel();

    /**
     * Gets the configured {@link EncryptionDeviceAccessLevel}
     *
     * @return the ID of the EncryptionDeviceAccessLevel
     */
    int getEncryptionDeviceAccessLevel();

    /**
     * Gets the used security properties for this Device
     *
     * @return the used security properties
     */
    TypedProperties getSecurityProperties();

}
