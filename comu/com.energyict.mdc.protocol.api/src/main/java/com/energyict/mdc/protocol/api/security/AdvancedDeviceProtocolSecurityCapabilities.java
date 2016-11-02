package com.energyict.mdc.protocol.api.security;

import java.util.List;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 5/02/2016 - 17:16
 */
public interface AdvancedDeviceProtocolSecurityCapabilities extends DeviceProtocolSecurityCapabilities {

    /**
     * A list of all supported security suites
     */
    List<SecuritySuite> getSecuritySuites();

    /**
     * A list of all supported security levels for requests.
     * Note that not all these levels are supported in all security suites.
     */
    List<RequestSecurityLevel> getRequestSecurityLevels();

    /**
     * A list of all supported security levels for responses
     * Note that not all these levels are supported in all security suites.
     */
    List<ResponseSecurityLevel> getResponseSecurityLevels();

}