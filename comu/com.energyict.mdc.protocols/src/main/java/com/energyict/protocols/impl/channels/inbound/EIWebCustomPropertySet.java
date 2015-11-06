package com.energyict.protocols.impl.channels.inbound;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.PersistenceSupport;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.StringFactory;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.protocols.impl.channels.CustomPropertySetTranslationKeys;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Provides an implementation for the {@link CustomPropertySet} interface
 * for the {@link EIWebConnectionType} class hierarcy.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-06 (16:19)
 */
public class EIWebCustomPropertySet implements CustomPropertySet<ConnectionType, EIWebConnectionProperties> {

    private final Thesaurus thesaurus;
    private final PropertySpecService propertySpecService;

    public EIWebCustomPropertySet(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super();
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
    }

    protected PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    @Override
    public String getId() {
        return this.getClass().getName();
    }

    @Override
    public String getName() {
        return this.thesaurus.getFormat(CustomPropertySetTranslationKeys.EIWEB_CUSTOM_PROPERTY_SET_NAME).format();
    }

    @Override
    public Class<ConnectionType> getDomainClass() {
        return ConnectionType.class;
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
    public PersistenceSupport<ConnectionType, EIWebConnectionProperties> getPersistenceSupport() {
        return new EIWebConnectionPropertiesPersistenceSupport();
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Arrays.asList(
                this.ipAddressPropertySpec(),
                this.macAddressPropertySpec());
    }

    private PropertySpec ipAddressPropertySpec() {
        return this.getPropertySpecService()
                .basicPropertySpec(
                        EIWebConnectionProperties.Fields.IP_ADDRESS.javaName(),
                        false,
                        new StringFactory());
    }

    private PropertySpec macAddressPropertySpec() {
        return this.getPropertySpecService()
                .basicPropertySpec(
                        EIWebConnectionProperties.Fields.MAC_ADDRESS.javaName(),
                        false,
                        new StringFactory());
    }

}