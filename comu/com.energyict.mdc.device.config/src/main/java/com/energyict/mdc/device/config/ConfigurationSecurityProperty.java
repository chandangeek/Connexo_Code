/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config;

import com.elster.jupiter.pki.SecurityAccessorType;

/**
 * Models a security property for a particular {@link SecurityPropertySet}
 *
 * @author Stijn Vanhoorelbeke
 * @since 21.04.17 - 10:34
 */
public interface ConfigurationSecurityProperty {

    /**
     * Gets the name of this {@link ConfigurationSecurityProperty}
     *
     * @return the name of this {@link ConfigurationSecurityProperty}
     */
    String getName();

    /**
     * Gets the {@link SecurityAccessorType} of this {@link ConfigurationSecurityProperty}
     *
     * @return Gets the {@link SecurityAccessorType} of this {@link ConfigurationSecurityProperty}
     */
    SecurityAccessorType getSecurityAccessorType();

    /**
     * Gets the {@link SecurityPropertySet} who owns this {@link ConfigurationSecurityProperty}
     *
     * @return Gets the {@link SecurityPropertySet} who owns this {@link ConfigurationSecurityProperty}
     */
    SecurityPropertySet getSecurityPropertySet();

}