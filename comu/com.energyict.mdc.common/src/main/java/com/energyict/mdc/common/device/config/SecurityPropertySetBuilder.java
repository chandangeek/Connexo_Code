/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.device.config;

import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.properties.PropertySpec;

import aQute.bnd.annotation.ConsumerType;

import java.util.Set;

@ConsumerType
public interface SecurityPropertySetBuilder {

    SecurityPropertySetBuilder authenticationLevel(int level);

    SecurityPropertySetBuilder encryptionLevel(int level);

    SecurityPropertySetBuilder client(Object client);

    SecurityPropertySetBuilder securitySuite(int suite);

    SecurityPropertySetBuilder requestSecurityLevel(int level);

    SecurityPropertySetBuilder responseSecurityLevel(int level);

    SecurityPropertySetBuilder addConfigurationSecurityProperty(String name, SecurityAccessorType keyAccessor);

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
