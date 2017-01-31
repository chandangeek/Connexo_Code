/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.impl.channels.sms;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.energyict.mdc.protocol.api.ConnectionProvider;
import com.energyict.protocols.impl.channels.CustomPropertySetTranslationKeys;
import com.energyict.protocols.impl.channels.TranslationKeys;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Provides an implementation for the {@link CustomPropertySet} interface
 * for the {@link OutboundProximusSmsConnectionType}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-06 (12:03)
 */
public class OutboundProximusCustomPropertySet implements CustomPropertySet<ConnectionProvider, OutboundProximusConnectionProperties> {

    private final Thesaurus thesaurus;
    private final PropertySpecService propertySpecService;

    public OutboundProximusCustomPropertySet(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super();
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
    }

    @Override
    public String getName() {
        return this.thesaurus.getFormat(CustomPropertySetTranslationKeys.OUTBOUND_PROXIMUS_CUSTOM_PROPERTY_SET_NAME).format();
    }

    @Override
    public Class<ConnectionProvider> getDomainClass() {
        return ConnectionProvider.class;
    }

    @Override
    public String getDomainClassDisplayName() {
        return this.thesaurus.getFormat(TranslationKeys.CONNECTION_PROVIDER_DOMAIN_NAME).format();
    }

    @Override
    public PersistenceSupport<ConnectionProvider, OutboundProximusConnectionProperties> getPersistenceSupport() {
        return new OutboundProximusConnectionPropertiesPersistenceSupport();
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
        EnumSet<OutboundProximusConnectionProperties.Fields> fields = EnumSet.allOf(OutboundProximusConnectionProperties.Fields.class);
        fields.remove(OutboundProximusConnectionProperties.Fields.CONNECTION_PROVIDER);
        return fields.stream()
                .map(prop -> prop.propertySpec(this.propertySpecService, this.thesaurus))
                .collect(Collectors.toList());
    }

}