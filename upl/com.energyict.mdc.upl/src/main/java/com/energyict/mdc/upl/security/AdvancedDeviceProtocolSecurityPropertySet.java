/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.upl.security;

/**
 * @author stijn
 * @since 04.05.17 - 16:48
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
