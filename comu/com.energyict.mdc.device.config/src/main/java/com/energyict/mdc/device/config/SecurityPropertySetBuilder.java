/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config;

import com.elster.jupiter.pki.KeyAccessorType;
import com.elster.jupiter.properties.PropertySpec;

import aQute.bnd.annotation.ProviderType;

import java.util.Set;

@ProviderType
public interface SecurityPropertySetBuilder {

    SecurityPropertySetBuilder authenticationLevel(int level);

    SecurityPropertySetBuilder encryptionLevel(int level);

    SecurityPropertySetBuilder client(String client);

    SecurityPropertySetBuilder securitySuite(int suite);

    SecurityPropertySetBuilder requestSecurityLevel(int level);

    SecurityPropertySetBuilder responseSecurityLevel(int level);

    SecurityPropertySetBuilder addConfigurationSecurityProperty(String name, KeyAccessorType keyAccessor);

    /**
     * Gets the Set of {@link PropertySpec}s that are the result
     * of the selected security levels present in
     * this {@llink SecurityPropertySetBuilder}.
     *
     * @return The Set of PropertySpecs
     */
    Set<PropertySpec> getPropertySpecs();

    SecurityPropertySet build();
}
