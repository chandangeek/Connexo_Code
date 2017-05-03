/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.security;

import com.elster.jupiter.properties.PropertySpec;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    @Override
    default List<PropertySpec> getSecurityPropertySpecs() {
        Set<PropertySpec> allSecurityPropertySpecs = new HashSet<>();
        for (SecuritySuite securitySuite : getSecuritySuites()) {
            securitySuite.getAuthenticationAccessLevels().forEach(accessLevel -> allSecurityPropertySpecs.addAll(accessLevel.getSecurityProperties()));
            securitySuite.getEncryptionAccessLevels().forEach(accessLevel -> allSecurityPropertySpecs.addAll(accessLevel.getSecurityProperties()));
            securitySuite.getRequestSecurityLevels().forEach(accessLevel -> allSecurityPropertySpecs.addAll(accessLevel.getSecurityProperties()));
            securitySuite.getResponseSecurityLevels().forEach(accessLevel -> allSecurityPropertySpecs.addAll(accessLevel.getSecurityProperties()));
        }
        return new ArrayList<>(allSecurityPropertySpecs);
    }
}