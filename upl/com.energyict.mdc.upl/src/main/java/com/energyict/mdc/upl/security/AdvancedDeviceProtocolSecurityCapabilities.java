package com.energyict.mdc.upl.security;

import java.util.List;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-01-04 (14:40)
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