/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest;

import com.elster.jupiter.pki.SecurityAccessor;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.pluggable.rest.PropertyDefaultValuesProvider;
import com.energyict.mdc.pluggable.rest.PropertyValuesResourceProvider;
import com.energyict.mdc.upl.TypedProperties;

import aQute.bnd.annotation.ProviderType;

import java.util.List;

@ProviderType
public interface SecurityAccessorInfoFactory {
    SecurityAccessorInfo from(SecurityAccessor<?> securityAccessor);

    SecurityAccessorInfo asCertificateProperties(List<PropertySpec> propertySpecs,
                                                 PropertyValuesResourceProvider aliasTypeAheadPropertyResourceProvider,
                                                 PropertyDefaultValuesProvider trustStoreValuesProvider);

    SecurityAccessorInfo asCertificate(SecurityAccessor<?> securityAccessor,
                                       PropertyValuesResourceProvider aliasTypeAheadPropertyResourceProvider,
                                       PropertyDefaultValuesProvider trustStoreValuesProvider);

    TypedProperties getPropertiesTempValue(SecurityAccessor<?> securityAccessor);

    TypedProperties getPropertiesActualValue(SecurityAccessor<?> securityAccessor);
}
