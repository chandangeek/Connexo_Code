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
 * Provides general security <b>capabilities</b> for devices that have either:
 * <ul>
 * <li>No password</li>
 * <li>A password</li>
 * </ul>
 * <p>
 * Copyrights EnergyICT
 * Date: 21/01/13
 * Time: 14:41
 */
public class NoOrPasswordSecuritySupport extends AbstractSecuritySupportAdapter implements DeviceProtocolSecurityCapabilities, LegacySecurityPropertyConverter {

    public NoOrPasswordSecuritySupport() {
        super();
    }

    @Inject
    public NoOrPasswordSecuritySupport(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(propertySpecService, thesaurus);
    }

    @Override
    public Optional<CustomPropertySet<Device, ? extends PersistentDomainExtension<Device>>> getCustomPropertySet() {
        return Optional.of(new NoOrPasswordCustomPropertySet(this.thesaurus, this.propertySpecService));
    }

    protected com.energyict.mdc.upl.security.DeviceProtocolSecurityCapabilities getSecuritySupport() {
        if (securitySupport == null) {
            securitySupport = new com.energyict.protocolimplv2.security.NoOrPasswordSecuritySupport(propertySpecService);
        }
        return securitySupport;
    }
}