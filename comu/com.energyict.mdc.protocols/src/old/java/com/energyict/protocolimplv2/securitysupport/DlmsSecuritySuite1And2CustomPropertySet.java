package com.energyict.protocolimplv2.securitysupport;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.protocol.api.security.SecurityCustomPropertySet;
import com.energyict.mdc.protocol.pluggable.impl.adapters.upl.UPLToConnexoPropertySpecAdapter;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimplv2.security.DlmsSecuritySuite1And2Support;
import com.energyict.protocols.mdc.services.impl.TranslationKeys;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides an implementation for the {@link DeviceProtocolSecurityCapabilities} interface
 * for DLMS security suite 1 and 2.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-12-09 (16:45)
 */
public class DlmsSecuritySuite1And2CustomPropertySet extends SecurityCustomPropertySet<DlmsSecuritySuite1And2Properties> {

    private final Thesaurus thesaurus;
    private final PropertySpecService propertySpecService;

    @Inject
    public DlmsSecuritySuite1And2CustomPropertySet(Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super();
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
    }

    @Override
    public String getDomainClassDisplayName() {
        return this.thesaurus.getFormat(TranslationKeys.SECURITY_PROPERTY_SET_CPS_DOMAIN_NAME).format();
    }

    @Override
    public DlmsSecuritySuite1And2PersistenceSupport getPersistenceSupport() {
        return new DlmsSecuritySuite1And2PersistenceSupport();
    }

    @Override
    public String getId() {
        return CustomPropertySetTranslationKeys.DLMS_SECURITY_SUITE_1AND2_CUSTOM_PROPERTY_SET_NAME.getKey();
    }

    @Override
    public String getName() {
        return this.thesaurus.getFormat(CustomPropertySetTranslationKeys.DLMS_SECURITY_SUITE_1AND2_CUSTOM_PROPERTY_SET_NAME).format();
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        com.energyict.mdc.upl.security.DeviceProtocolSecurityCapabilities securitySupport = new DlmsSecuritySuite1And2Support(propertySpecService);

        return securitySupport.getSecurityProperties()
                .stream()
                .map(UPLToConnexoPropertySpecAdapter::new)
                .collect(Collectors.toList());
    }
}