package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.ConnectionType;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.properties.PropertySpec;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-05 (13:16)
 */
public class IpConnectionCustomPropertySet implements CustomPropertySet<ConnectionType, IpConnectionPropertyValues> {

    private final PropertySpecService propertySpecService;

    public IpConnectionCustomPropertySet(PropertySpecService propertySpecService) {
        super();
        this.propertySpecService = propertySpecService;
    }

    @Override
    public String getName() {
        return "Ip connection for testing purposes only";
    }

    @Override
    public Class<ConnectionType> getDomainClass() {
        return ConnectionType.class;
    }

    @Override
    public PersistenceSupport<ConnectionType, IpConnectionPropertyValues> getPersistenceSupport() {
        return new IpConnectionPersistenceSupport();
    }

    @Override
    public boolean isRequired() {
        return true;
    }

    @Override
    public boolean isVersioned() {
        return true;
    }

    @Override
    public Set<ViewPrivilege> defaultViewPrivileges() {
        return EnumSet.allOf(ViewPrivilege.class);
    }

    @Override
    public Set<EditPrivilege> defaultEditPrivileges() {
        return EnumSet.allOf(EditPrivilege.class);
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Stream
                .of(IpConnectionProperties.values())
                .map(each -> each.propertySpec(this.propertySpecService))
                .collect(Collectors.toList());
    }

}