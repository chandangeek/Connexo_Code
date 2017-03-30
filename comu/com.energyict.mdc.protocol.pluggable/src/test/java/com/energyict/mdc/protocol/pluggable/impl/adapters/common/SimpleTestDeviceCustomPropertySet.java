/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.security.SecurityCustomPropertySet;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides an implementation for the {@link CustomPropertySet} interface for {@link SimpleTestDeviceSecuritySupport}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-20 (10:57)
 */
public class SimpleTestDeviceCustomPropertySet extends SecurityCustomPropertySet<SimpleTestDeviceSecurityProperties> {

    private final PropertySpecService propertySpecService;

    public SimpleTestDeviceCustomPropertySet(PropertySpecService propertySpecService) {
        super();
        this.propertySpecService = propertySpecService;
    }

    @Override
    public String getDomainClassDisplayName() {
        return this.getDomainClass().getName();
    }

    @Override
    public SimpleTestDevicePersistenceSupport getPersistenceSupport() {
        return new SimpleTestDevicePersistenceSupport();
    }

    @Override
    public String getId() {
        return SimpleTestDeviceCustomPropertySet.class.getName();
    }

    @Override
    public String getName() {
        return SimpleTestDeviceCustomPropertySet.class.getSimpleName();
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Stream
                .of(SimpleTestDeviceSecurityProperties.ActualFields.values())
                .map(field -> field.propertySpec(this.propertySpecService))
                .collect(Collectors.toList());
    }

}