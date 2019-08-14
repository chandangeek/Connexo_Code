/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config;

import com.energyict.mdc.common.protocol.ProtocolDialectConfigurationProperties;

import aQute.bnd.annotation.ProviderType;

/**
 * Models a single property of a {@link ProtocolDialectConfigurationProperties}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-02-18 (16:41)
 */
@ProviderType
public interface ProtocolDialectConfigurationProperty {

    /**
     * Gets the name of the single property.
     *
     * @return The name
     */
    public String getName();

    /**
     * Gets the value of the single property.
     *
     * @return The value
     */
    public String getValue();

    public ProtocolDialectConfigurationProperties getProtocolDialectConfigurationProperties();

}