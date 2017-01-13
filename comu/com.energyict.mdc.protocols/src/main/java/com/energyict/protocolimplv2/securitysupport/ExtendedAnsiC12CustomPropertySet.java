package com.energyict.protocolimplv2.securitysupport;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.protocol.api.security.SecurityCustomPropertySet;
import com.energyict.mdc.protocol.pluggable.impl.adapters.upl.UPLToConnexoPropertySpecAdapter;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocols.mdc.services.impl.TranslationKeys;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides an implementation for the {@link CustomPropertySet} interface for {@link ExtendedAnsiC12SecuritySupport}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-19 (13:50)
 */
public class ExtendedAnsiC12CustomPropertySet extends SecurityCustomPropertySet<AnsiC12SecurityProperties> {

    private final Thesaurus thesaurus;
    private final PropertySpecService propertySpecService;

    @Inject
    public ExtendedAnsiC12CustomPropertySet(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super();
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
    }

    @Override
    public String getDomainClassDisplayName() {
        return this.thesaurus.getFormat(TranslationKeys.SECURITY_PROPERTY_SET_CPS_DOMAIN_NAME).format();
    }

    @Override
    public AnsiC12PersistenceSupport getPersistenceSupport() {
        return new AnsiC12PersistenceSupport();
    }

    @Override
    public String getId() {
        return CustomPropertySetTranslationKeys.EXTENDED_ANSI_C12_CUSTOM_PROPERTY_SET_NAME.getKey();
    }

    @Override
    public String getName() {
        return this.thesaurus.getFormat(CustomPropertySetTranslationKeys.EXTENDED_ANSI_C12_CUSTOM_PROPERTY_SET_NAME).format();
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        //The property specs for this security set are defined in the 9.1 protocol code base
        return new com.energyict.protocolimplv2.security.ExtendedAnsiC12SecuritySupport(propertySpecService)
                .getSecurityProperties()
                .stream()
                .map(UPLToConnexoPropertySpecAdapter::new)
                .collect(Collectors.toList());
    }
}