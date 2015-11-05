package com.energyict.protocols.impl.channels.serial;

import com.energyict.mdc.io.SerialComponentService;
import com.energyict.mdc.protocol.api.ConnectionType;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;

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
public class SioSerialCustomPropertySet implements CustomPropertySet<ConnectionType, SioSerialConnectionProperties> {

    private final Thesaurus thesaurus;
    private final SerialComponentService serialComponentService;

    public SioSerialCustomPropertySet(Thesaurus thesaurus, SerialComponentService serialComponentService) {
        super();
        this.thesaurus = thesaurus;
        this.serialComponentService = serialComponentService;
    }

    @Override
    public String getName() {
        return this.thesaurus.getFormat(SioSerialTranslationKeys.CUSTOM_PROPERTY_SET_NAME).format();
    }

    @Override
    public Class<ConnectionType> getDomainClass() {
        return ConnectionType.class;
    }

    @Override
    public PersistenceSupport<ConnectionType, SioSerialConnectionProperties> getPersistenceSupport() {
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