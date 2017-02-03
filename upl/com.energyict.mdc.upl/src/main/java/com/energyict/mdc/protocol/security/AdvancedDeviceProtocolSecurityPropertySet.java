package com.energyict.mdc.protocol.security;

import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.upl.security.RequestSecurityLevel;
import com.energyict.mdc.upl.security.ResponseSecurityLevel;
import com.energyict.mdc.upl.security.SecuritySuite;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 8/02/2016 - 11:33
 */
public interface AdvancedDeviceProtocolSecurityPropertySet extends DeviceProtocolSecurityPropertySet {

    /**
     * Gets the configured {@link SecuritySuite}
     */
    int getSecuritySuite();

    /**
     * Gets the configured {@link RequestSecurityLevel}
     */
    int getRequestSecurityLevel();

    /**
     * Gets the configured {@link ResponseSecurityLevel}
     */
    int getResponseSecurityLevel();

}
