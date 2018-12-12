/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.security;

import com.elster.jupiter.properties.PropertySpec;

import java.util.Set;

/**
 * Provides security {@link PropertySpec}s for a physical device.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-18 (12:03)
 */
public interface SecurityPropertySpecProvider {

    /**
     * Gets the Set of {@link PropertySpec}s that are the result
     * of the selected {@link AuthenticationDeviceAccessLevel authentication}
     * and {@link EncryptionDeviceAccessLevel encryption} levels.
     *
     * @return The Set of PropertySpecs
     */
    Set<PropertySpec> getPropertySpecs();

}