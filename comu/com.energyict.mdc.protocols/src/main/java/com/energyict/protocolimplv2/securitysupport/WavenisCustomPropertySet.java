package com.energyict.protocolimplv2.securitysupport;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.protocol.api.security.SecurityCustomPropertySet;
import com.energyict.mdc.protocol.pluggable.adapters.upl.UPLToConnexoPropertySpecAdapter;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityCapabilities;
import com.energyict.protocols.mdc.services.impl.TranslationKeys;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides an implementation for the {@link CustomPropertySet} interface for {@link WavenisSecuritySupport}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-19 (13:41)
 */
public class WavenisCustomPropertySet extends SecurityCustomPropertySet<WavenisSecurityProperties> {

    private final Thesaurus thesaurus;
    private final PropertySpecService propertySpecService;

    @Inject
    public WavenisCustomPropertySet(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super();
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
    }

    @Override
    public String getDomainClassDisplayName() {
        return this.thesaurus.getFormat(TranslationKeys.SECURITY_PROPERTY_SET_CPS_DOMAIN_NAME).format();
    }

    @Override
    public WavenisPersistenceSupport getPersistenceSupport() {
        return new WavenisPersistenceSupport();
    }

    @Override
    public String getId() {
        return CustomPropertySetTranslationKeys.WAVENIS_CUSTOM_PROPERTY_SET_NAME.getKey();
    }

    @Override
    public String getName() {
        return this.thesaurus.getFormat(CustomPropertySetTranslationKeys.WAVENIS_CUSTOM_PROPERTY_SET_NAME).format();
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        //The property specs for this security set are defined in the 9.1 protocol code base
        DeviceProtocolSecurityCapabilities securitySupport = new com.energyict.protocolimplv2.security.WavenisSecuritySupport(propertySpecService);

        return securitySupport.getSecurityProperties()
                .stream()
                .map(UPLToConnexoPropertySpecAdapter::new)
                .collect(Collectors.toList());
    }
}