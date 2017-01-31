/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.impl.channels.serial;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.io.SerialComponentService;
import com.energyict.mdc.protocol.api.ConnectionProvider;
import com.energyict.protocols.impl.channels.CustomPropertySetTranslationKeys;
import com.energyict.protocols.impl.channels.TranslationKeys;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Provides an implementation for the {@link CustomPropertySet} interface
 * that can be used for all the "serial" connection types
 * that use one of the underlying serial libraries that
 * are supported by the mdc.io bundle.
 * Ideally, this class should be in the mdc.io bundle so that a
 * {@link com.energyict.mdc.io.SerialComponentService}
 * could actually return the CustomPropertySet for the properties
 * that it defines. However, that would have introduced a cyclic dependency
 * between the protocol.api bundle and the mdc.io bundle that we were
 * not happy resolving due to time constraints.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-04 (13:09)
 */
public class SioSerialCustomPropertySet implements CustomPropertySet<ConnectionProvider, SioSerialConnectionProperties> {

    private final String id;
    private final Thesaurus thesaurus;
    private final SerialComponentService serialComponentService;

    public SioSerialCustomPropertySet(String id, Thesaurus thesaurus, SerialComponentService serialComponentService) {
        super();
        this.id = id;
        this.thesaurus = thesaurus;
        this.serialComponentService = serialComponentService;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.thesaurus.getFormat(CustomPropertySetTranslationKeys.SIO_SERIAL_CUSTOM_PROPERTY_SET_NAME).format();
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
    public PersistenceSupport<ConnectionProvider, SioSerialConnectionProperties> getPersistenceSupport() {
        return new SioSerialConnectionPropertiesPersistenceSupport();
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
        return this.serialComponentService.getPropertySpecs();
    }

}