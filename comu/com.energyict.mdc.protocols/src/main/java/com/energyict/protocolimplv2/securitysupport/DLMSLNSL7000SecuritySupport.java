package com.energyict.protocolimplv2.securitysupport;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.security.LegacySecurityPropertyConverter;

import javax.inject.Inject;
import java.util.Optional;

/**
 * Provides general security <b>capabilities</b> for the DLMSLNSL7000 protocol.
 * <p>
 * Copyrights EnergyICT
 * Date: 10/01/13
 * Time: 16:39
 */
public class DLMSLNSL7000SecuritySupport extends AbstractSecuritySupportAdapter implements DeviceProtocolSecurityCapabilities, LegacySecurityPropertyConverter {

    // For unit testing purposes
    @Inject
    public DLMSLNSL7000SecuritySupport(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(propertySpecService, thesaurus);
    }

    @Override
    public Optional<CustomPropertySet<Device, ? extends PersistentDomainExtension<Device>>> getCustomPropertySet() {
        return Optional.of(new DlmsSecurityCustomPropertySet(this.thesaurus, this.propertySpecService));
    }

    protected com.energyict.mdc.upl.security.DeviceProtocolSecurityCapabilities getSecuritySupport() {
        if (securitySupport == null) {
            securitySupport = new com.energyict.protocolimplv2.security.DLMSLNSL7000SecuritySupport(propertySpecService);
        }
        return securitySupport;
    }
}