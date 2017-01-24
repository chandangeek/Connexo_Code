package com.energyict.protocolimplv2.securitysupport;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.protocol.api.security.SecurityCustomPropertySet;
import com.energyict.mdc.protocol.pluggable.impl.adapters.upl.UPLToConnexoPropertySpecAdapter;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityCapabilities;
import com.energyict.protocols.mdc.services.impl.TranslationKeys;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides an implementation for the {@link CustomPropertySet} interface for {@link DlmsSecuritySupportPerClient}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-19 (15:27)
 */
public class DlmsSecurityPerClientCustomPropertySet extends SecurityCustomPropertySet<DlmsSecurityPerClientProperties> {

    private final Thesaurus thesaurus;

    @Inject
    public DlmsSecurityPerClientCustomPropertySet(Thesaurus thesaurus) {
        super();
        this.thesaurus = thesaurus;
    }

    @Override
    public String getDomainClassDisplayName() {
        return this.thesaurus.getFormat(TranslationKeys.SECURITY_PROPERTY_SET_CPS_DOMAIN_NAME).format();
    }

    @Override
    public DlmsSecurityPerClientPersistenceSupport getPersistenceSupport() {
        return new DlmsSecurityPerClientPersistenceSupport();
    }

    @Override
    public String getId() {
        return CustomPropertySetTranslationKeys.DLMS_SECURITY_PER_CLIENT_CUSTOM_PROPERTY_SET_NAME.getKey();
    }

    @Override
    public String getName() {
        return this.thesaurus.getFormat(CustomPropertySetTranslationKeys.DLMS_SECURITY_PER_CLIENT_CUSTOM_PROPERTY_SET_NAME).format();
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        //The property specs for this security set are defined in the 9.1 protocol code base
        DeviceProtocolSecurityCapabilities securitySupport = new com.energyict.protocolimplv2.security.DlmsSecuritySupportPerClient();

        return securitySupport.getSecurityProperties()
                .stream()
                .map(UPLToConnexoPropertySpecAdapter::new)
                .collect(Collectors.toList());
    }
}