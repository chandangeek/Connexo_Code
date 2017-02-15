/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.impl.channels.inbound;

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
import com.energyict.protocols.impl.channels.ip.OutboundIpConnectionType;
import com.energyict.protocols.naming.ConnectionTypePropertySpecName;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Provides an implementation for the {@link CustomPropertySet} interface
 * for the {@link OutboundIpConnectionType} class hierarcy.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-06 (12:03)
 */
public class CTRInboundDialHomeIdCustomPropertySet implements CustomPropertySet<ConnectionProvider, CTRInboundDialHomeIdConnectionProperties> {

    private final Thesaurus thesaurus;
    private final PropertySpecService propertySpecService;

    public CTRInboundDialHomeIdCustomPropertySet(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super();
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
    }

    protected PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    @Override
    public String getId() {
        return CTRInboundDialHomeIdConnectionType.class.getSimpleName();
    }

    @Override
    public String getName() {
        return this.thesaurus.getFormat(CustomPropertySetTranslationKeys.CTR_INBOUND_DIAL_HOME_ID_CUSTOM_PROPERTY_SET_NAME).format();
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
    public PersistenceSupport<ConnectionProvider, CTRInboundDialHomeIdConnectionProperties> getPersistenceSupport() {
        return new CTRInboundDialHomeIdConnectionPropertiesPersistenceSupport();
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Collections.singletonList(this.dialHomeIdPropertySpec());
    }

    private PropertySpec dialHomeIdPropertySpec() {
        return this.getPropertySpecService()
                .stringSpec()
                .named(CTRInboundDialHomeIdConnectionProperties.Fields.DIAL_HOME_ID.propertySpecName(), ConnectionTypePropertySpecName.CTR_INBOUND_DIAL_HOME_ID)
                .fromThesaurus(this.thesaurus)
                .markRequired()
                .finish();
    }

}