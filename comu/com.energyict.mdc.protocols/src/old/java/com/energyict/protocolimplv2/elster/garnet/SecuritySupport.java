package com.energyict.protocolimplv2.elster.garnet;


import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimplv2.securitysupport.AbstractSecuritySupportAdapter;

import javax.inject.Inject;
import java.util.Optional;

/**
 * @author sva
 * @since 18/06/2014 - 10:54
 */
public class SecuritySupport extends AbstractSecuritySupportAdapter implements DeviceProtocolSecurityCapabilities {

    public SecuritySupport() {
        super();
    }

    @Inject
    public SecuritySupport(PropertySpecService propertySpecService, Thesaurus thesaurus) {
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