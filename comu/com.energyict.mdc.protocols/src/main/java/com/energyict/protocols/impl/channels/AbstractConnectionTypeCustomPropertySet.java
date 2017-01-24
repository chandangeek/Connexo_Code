package com.energyict.protocols.impl.channels;

import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.protocol.api.ConnectionProvider;
import com.energyict.mdc.protocol.pluggable.impl.adapters.upl.UPLToConnexoPropertySpecAdapter;
import com.energyict.mdc.upl.properties.PropertySpecService;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 23/01/2017 - 14:23
 */
public abstract class AbstractConnectionTypeCustomPropertySet {

    protected volatile Thesaurus thesaurus;
    protected volatile PropertySpecService propertySpecService;

    @Inject
    public AbstractConnectionTypeCustomPropertySet(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
    }

    @Reference
    public void setThesaurus(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    /**
     * The connection type implementation, from the 9.1 protocol code
     */
    public abstract com.energyict.mdc.io.ConnectionType getConnectionTypeSupport();

    /**
     * Return the property specs that were provided by the connection type impl of the 9.1 protocol code
     */
    public List<PropertySpec> getPropertySpecs() {
        return getConnectionTypeSupport()
                .getUPLPropertySpecs()
                .stream()
                .map(UPLToConnexoPropertySpecAdapter::new)
                .collect(Collectors.toList());
    }

    public Class<ConnectionProvider> getDomainClass() {
        return ConnectionProvider.class;
    }

    public String getDomainClassDisplayName() {
        return this.thesaurus.getFormat(TranslationKeys.CONNECTION_PROVIDER_DOMAIN_NAME).format();
    }

    public boolean isRequired() {
        return true;
    }

    public boolean isVersioned() {
        return true;
    }

    public Set<ViewPrivilege> defaultViewPrivileges() {
        return EnumSet.allOf(ViewPrivilege.class);
    }

    public Set<EditPrivilege> defaultEditPrivileges() {
        return EnumSet.allOf(EditPrivilege.class);
    }
}