package com.energyict.protocolimplv2.securitysupport;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.protocol.api.security.SecurityCustomPropertySet;
import com.energyict.mdc.protocol.pluggable.impl.adapters.upl.UPLToConnexoPropertySpecAdapter;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityCapabilities;
import com.energyict.protocols.mdc.services.impl.TranslationKeys;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides an implementation for the {@link CustomPropertySet} interface for {@link BasicAuthenticationSecurityProperties}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-20 (10:18)
 */
public class BasicAuthenticationCustomPropertySet extends SecurityCustomPropertySet<BasicAuthenticationSecurityProperties> {

    private final Thesaurus thesaurus;
    private final PropertySpecService propertySpecService;

    @Inject
    public BasicAuthenticationCustomPropertySet(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super();
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
    }

    @Override
    public String getDomainClassDisplayName() {
        return this.thesaurus.getFormat(TranslationKeys.SECURITY_PROPERTY_SET_CPS_DOMAIN_NAME).format();
    }

    @Override
    public BasicAuthenticationPersistenceSupport getPersistenceSupport() {
        return new BasicAuthenticationPersistenceSupport();
    }

    @Override
    public String getId() {
        return CustomPropertySetTranslationKeys.BASIC_AUTHENTICATION_CUSTOM_PROPERTY_SET_NAME.getKey();
    }

    @Override
    public String getName() {
        return this.thesaurus.getFormat(CustomPropertySetTranslationKeys.BASIC_AUTHENTICATION_CUSTOM_PROPERTY_SET_NAME).format();
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        //The property specs for this security set are defined in the 9.1 protocol code base
        DeviceProtocolSecurityCapabilities securitySupport = new com.energyict.protocolimplv2.security.PasswordWithUserIdentificationSecuritySupport(propertySpecService);

        return securitySupport.getSecurityProperties()
                .stream()
                .map(UPLToConnexoPropertySpecAdapter::new)
                .collect(Collectors.toList());
    }
}