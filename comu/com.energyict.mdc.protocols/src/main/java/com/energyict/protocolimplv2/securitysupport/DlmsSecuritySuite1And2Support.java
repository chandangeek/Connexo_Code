package com.energyict.protocolimplv2.securitysupport;


import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.protocol.api.security.AdvancedDeviceProtocolSecurityCapabilities;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.security.LegacySecurityPropertyConverter;
import com.energyict.mdc.upl.security.RequestSecurityLevel;
import com.energyict.mdc.upl.security.ResponseSecurityLevel;
import com.energyict.mdc.upl.security.SecuritySuite;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

/**
 * @author khe
 * @since 28/01/2016 - 17:39
 */
public class DlmsSecuritySuite1And2Support extends AbstractSecuritySupportAdapter implements LegacySecurityPropertyConverter, AdvancedDeviceProtocolSecurityCapabilities {

    @Inject
    public DlmsSecuritySuite1And2Support(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(propertySpecService, thesaurus);
    }

    @Override
    public List<SecuritySuite> getSecuritySuites() {
        return getSecuritySupport().getSecuritySuites();
    }

    @Override
    public List<RequestSecurityLevel> getRequestSecurityLevels() {
        return getSecuritySupport().getRequestSecurityLevels();
    }

    @Override
    public List<ResponseSecurityLevel> getResponseSecurityLevels() {
        return getSecuritySupport().getResponseSecurityLevels();
    }

    @Override
    public Optional<CustomPropertySet<Device, ? extends PersistentDomainExtension<Device>>> getCustomPropertySet() {
        return Optional.of(new DlmsSecuritySuite1And2CustomPropertySet(this.thesaurus, this.propertySpecService));
    }

    protected com.energyict.mdc.upl.security.AdvancedDeviceProtocolSecurityCapabilities getSecuritySupport() {
        if (securitySupport == null) {
            securitySupport = new com.energyict.protocolimplv2.security.DlmsSecuritySuite1And2Support(propertySpecService);
        }
        return (com.energyict.mdc.upl.security.AdvancedDeviceProtocolSecurityCapabilities) securitySupport;
    }
}