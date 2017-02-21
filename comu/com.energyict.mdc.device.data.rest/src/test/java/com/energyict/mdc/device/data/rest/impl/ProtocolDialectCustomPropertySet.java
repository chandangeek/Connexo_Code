/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.impl.PropertySpecServiceImpl;
import com.energyict.mdc.protocol.api.DeviceProtocolDialectPropertyProvider;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-01-06 (16:26)
 */
public class ProtocolDialectCustomPropertySet implements CustomPropertySet<DeviceProtocolDialectPropertyProvider, PersistentProtocolDialectProperties> {
    @Override
    public String getId() {
        return ProtocolDialectCustomPropertySet.class.getSimpleName() + ProtocolDialectCustomPropertySet.class.getSimpleName();
    }

    @Override
    public String getName() {
        return ProtocolDialectCustomPropertySet.class.getName();
    }

    @Override
    public Class<DeviceProtocolDialectPropertyProvider> getDomainClass() {
        return DeviceProtocolDialectPropertyProvider.class;
    }

    @Override
    public String getDomainClassDisplayName() {
        return this.getDomainClass().getSimpleName();
    }

    @Override
    public PersistenceSupport<DeviceProtocolDialectPropertyProvider, PersistentProtocolDialectProperties> getPersistenceSupport() {
        return new ProtocolDialectPropertiesPersistenceSupport();
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
        PropertySpecServiceImpl propertySpecService = new PropertySpecServiceImpl();
        return Arrays.asList(
                propertySpecService
                        .stringSpec()
                        .named(TestProtocol.REQUIRED_PROPERTY_NAME, TestProtocol.REQUIRED_PROPERTY_NAME)
                        .describedAs("Description for required property")
                        .markRequired()
                        .finish(),
                propertySpecService
                        .stringSpec()
                        .named(TestProtocol.OPTIONAL_PROPERTY_NAME, TestProtocol.OPTIONAL_PROPERTY_NAME)
                        .describedAs("Description for optional property")
                        .finish());
    }

}