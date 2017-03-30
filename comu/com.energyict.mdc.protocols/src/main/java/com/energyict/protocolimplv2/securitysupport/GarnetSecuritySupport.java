package com.energyict.protocolimplv2.securitysupport;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.security.LegacySecurityPropertyConverter;
import com.energyict.protocolimplv2.elster.garnet.GarnetSecuritySupportCustomPropertySet;

import javax.inject.Inject;
import java.util.Optional;

/**
 * Provides general security <b>capabilities</b> for a Garnet protocol.
 * <p>
 *
 * Date: 10/01/13
 * Time: 16:39
 */
public class GarnetSecuritySupport extends AbstractSecuritySupportAdapter implements DeviceProtocolSecurityCapabilities, LegacySecurityPropertyConverter {

    // For unit testing purposes
    @Inject
    public GarnetSecuritySupport(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(propertySpecService, thesaurus);
    }

    @Override
    public Optional<CustomPropertySet<Device, ? extends PersistentDomainExtension<Device>>> getCustomPropertySet() {
        return Optional.of(new GarnetSecuritySupportCustomPropertySet(this.thesaurus, this.propertySpecService));
    }

    protected com.energyict.mdc.upl.security.DeviceProtocolSecurityCapabilities getSecuritySupport() {
        if (securitySupport == null) {
            securitySupport = new com.energyict.protocolimplv2.elster.garnet.GarnetSecuritySupport(propertySpecService);
        }
        return securitySupport;
    }
}