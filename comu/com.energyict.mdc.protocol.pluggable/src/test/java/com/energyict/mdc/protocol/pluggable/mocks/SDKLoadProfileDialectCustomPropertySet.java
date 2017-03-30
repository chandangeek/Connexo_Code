/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.mocks;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.DeviceProtocolDialectPropertyProvider;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides an implementation for the {@link CustomPropertySet} interface for {@link SDKLoadProfileProtocolDialect}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-27 (09:37)
 */
class SDKLoadProfileDialectCustomPropertySet implements CustomPropertySet<DeviceProtocolDialectPropertyProvider, SDKLoadProfileDialectProperties> {

    private final PropertySpecService propertySpecService;

    SDKLoadProfileDialectCustomPropertySet(PropertySpecService propertySpecService) {
        super();
        this.propertySpecService = propertySpecService;
    }

    @Override
    public String getId() {
        return SDKLoadProfileProtocolDialect.class.getName();
    }

    @Override
    public String getName() {
        return "SDK dialect for loadProfile testing";
    }

    @Override
    public Class<DeviceProtocolDialectPropertyProvider> getDomainClass() {
        return DeviceProtocolDialectPropertyProvider.class;
    }

    @Override
    public String getDomainClassDisplayName() {
        return this.getDomainClass().getName();
    }

    @Override
    public PersistenceSupport<DeviceProtocolDialectPropertyProvider, SDKLoadProfileDialectProperties> getPersistenceSupport() {
        return new SDKLoadProfileDialectPropertyPersistenceSupport();
    }

    @Override
    public boolean isRequired() {
        return false;
    }

    @Override
    public boolean isVersioned() {
        return true;
    }

    @Override
    public Set<ViewPrivilege> defaultViewPrivileges() {
        return EnumSet.noneOf(ViewPrivilege.class);
    }

    @Override
    public Set<EditPrivilege> defaultEditPrivileges() {
        return EnumSet.noneOf(EditPrivilege.class);
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Stream
                .of(SDKLoadProfileDialectProperties.ActualFields.values())
                .map(field -> field.propertySpec(this.propertySpecService))
                .collect(Collectors.toList());
    }

}