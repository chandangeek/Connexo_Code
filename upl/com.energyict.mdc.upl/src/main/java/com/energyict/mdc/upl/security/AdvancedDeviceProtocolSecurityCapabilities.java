package com.energyict.mdc.upl.security;

import com.energyict.mdc.upl.properties.PropertySpec;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    @Override
    default List<PropertySpec> getSecurityProperties() {
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