/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.security;

import java.util.List;

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