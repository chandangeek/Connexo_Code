/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.security.SecurityCustomPropertySet;

import java.util.Arrays;
import java.util.List;

/**
 * Provides an implementation for the {@link CustomPropertySet} interface for {@link BasicAuthenticationSecurityProperties}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-20 (11:13)
 */
public class BasicAuthenticationCustomPropertySet extends SecurityCustomPropertySet<BasicAuthenticationSecurityProperties> {

    private final PropertySpecService propertySpecService;

    public BasicAuthenticationCustomPropertySet(PropertySpecService propertySpecService) {
        super();
        this.propertySpecService = propertySpecService;
    }

    @Override
    public String getDomainClassDisplayName() {
        return this.getDomainClass().getName();
    }

    @Override
    public BasicAuthenticationPersistenceSupport getPersistenceSupport() {
        return new BasicAuthenticationPersistenceSupport();
    }

    @Override
    public String getId() {
        return BasicAuthenticationCustomPropertySet.class.getName();
    }

    @Override
    public String getName() {
        return BasicAuthenticationCustomPropertySet.class.getSimpleName();
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Arrays.asList(
                BasicAuthenticationSecurityProperties.ActualFields.USER_NAME.propertySpec(this.propertySpecService),
                BasicAuthenticationSecurityProperties.ActualFields.PASSWORD.propertySpec(this.propertySpecService));
    }

}