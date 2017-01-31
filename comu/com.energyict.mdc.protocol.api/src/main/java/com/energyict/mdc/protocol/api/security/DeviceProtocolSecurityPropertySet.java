/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.security;

import com.energyict.mdc.common.TypedProperties;

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
