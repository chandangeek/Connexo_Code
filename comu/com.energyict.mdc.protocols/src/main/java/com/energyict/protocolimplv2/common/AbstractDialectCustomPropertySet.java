package com.energyict.protocolimplv2.common;

import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.protocol.api.DeviceProtocolDialectPropertyProvider;
import com.energyict.mdc.protocol.pluggable.impl.adapters.upl.UPLToConnexoPropertySpecAdapter;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocols.mdc.services.impl.TranslationKeys;
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
 * @since 12/01/2017 - 10:15
 */
public abstract class AbstractDialectCustomPropertySet {

    protected volatile PropertySpecService propertySpecService;
    private volatile Thesaurus thesaurus;

    @Inject
    public AbstractDialectCustomPropertySet(Thesaurus thesaurus, PropertySpecService propertySpecService) {
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
     * The dialect class from the 9.1 protocol code, providing the property specs.
     */
    protected abstract com.energyict.mdc.upl.DeviceProtocolDialect getDeviceProtocolDialect();

    public List<PropertySpec> getPropertySpecs() {
        //The property specs of this dialect are provided by the 9.1 protocol code.
        return getDeviceProtocolDialect().getUPLPropertySpecs().stream().map(UPLToConnexoPropertySpecAdapter::new).collect(Collectors.toList());
    }

    public String getId() {
        return getDeviceProtocolDialect().getClass().getName();
    }

    public String getName() {
        return getDeviceProtocolDialect().getDeviceProtocolDialectDisplayName();
    }

    public Class<DeviceProtocolDialectPropertyProvider> getDomainClass() {
        return DeviceProtocolDialectPropertyProvider.class;
    }

    public String getDomainClassDisplayName() {
        return this.thesaurus.getFormat(TranslationKeys.DIALECT_CPS_DOMAIN_NAME).format();
    }

    public boolean isRequired() {
        return false;
    }

    public boolean isVersioned() {
        return true;
    }

    public Set<ViewPrivilege> defaultViewPrivileges() {
        return EnumSet.noneOf(ViewPrivilege.class);
    }

    public Set<EditPrivilege> defaultEditPrivileges() {
        return EnumSet.noneOf(EditPrivilege.class);
    }
}