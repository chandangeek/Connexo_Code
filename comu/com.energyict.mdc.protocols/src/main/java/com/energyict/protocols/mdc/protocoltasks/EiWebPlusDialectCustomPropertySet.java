/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.mdc.protocoltasks;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.DeviceProtocolDialectPropertyProvider;
import com.energyict.protocols.mdc.services.impl.TranslationKeys;

import com.energyict.protocolimplv2.DeviceProtocolDialectName;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides an implementation for the {@link CustomPropertySet} interface for {@link EiWebPlusDialect}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-27 (08:59)
 */
public class EiWebPlusDialectCustomPropertySet implements CustomPropertySet<DeviceProtocolDialectPropertyProvider, EiWebPlusDialectProperties> {

    private final Thesaurus thesaurus;
    private final PropertySpecService propertySpecService;

    public EiWebPlusDialectCustomPropertySet(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super();
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
    }

    @Override
    public String getId() {
        return EiWebPlusDialect.class.getName();
    }

    @Override
    public String getName() {
        return this.thesaurus.getFormat(DeviceProtocolDialectName.EIWEBPLUS).format();
    }

    @Override
    public Class<DeviceProtocolDialectPropertyProvider> getDomainClass() {
        return DeviceProtocolDialectPropertyProvider.class;
    }

    @Override
    public String getDomainClassDisplayName() {
        return this.thesaurus.getFormat(TranslationKeys.DIALECT_CPS_DOMAIN_NAME).format();
    }

    @Override
    public PersistenceSupport<DeviceProtocolDialectPropertyProvider, EiWebPlusDialectProperties> getPersistenceSupport() {
        return new EiWebPlusDialectPropertyPersistenceSupport();
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
                .of(EiWebPlusDialectProperties.ActualFields.values())
                .map(field -> field.propertySpec(this.propertySpecService, this.thesaurus))
                .collect(Collectors.toList());
    }

}