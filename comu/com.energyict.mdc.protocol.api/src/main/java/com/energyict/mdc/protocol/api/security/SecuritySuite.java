/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.security;

import java.io.Serializable;
import java.util.List;

public interface SecuritySuite extends Serializable, DeviceAccessLevel {

    /**
     * A list of {@link EncryptionDeviceAccessLevel}s supported by this security suite
     */
    List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels();

    /**
     * A list of {@link AuthenticationDeviceAccessLevel}s supported by this security suite
     */
    List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels();

    /**
     * A list of all supported security levels for requests
     */
    List<RequestSecurityLevel> getRequestSecurityLevels();

    /**
     * A list of all supported security levels for responses
     */
    List<ResponseSecurityLevel> getResponseSecurityLevels();

}