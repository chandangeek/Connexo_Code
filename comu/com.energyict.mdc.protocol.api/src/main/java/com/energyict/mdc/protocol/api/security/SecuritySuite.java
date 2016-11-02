package com.energyict.mdc.protocol.api.security;

import java.io.Serializable;
import java.util.List;

/**
 * A security set can have one or more security suites that each specifies a number of supported security levels.
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 5/02/2016 - 17:20
 */
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