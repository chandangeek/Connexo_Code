package com.energyict.protocolimplv2.securitysupport;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.security.LegacySecurityPropertyConverter;

import javax.inject.Inject;

/**
 * Provides general security <b>capabilities</b> for DeviceProtocols
 * that use a single password to do authentication/encryption
 * <p>
 *
 * Date: 11/01/13
 * Time: 14:47
 */
public class SimplePasswordSecuritySupport extends AbstractSecuritySupportAdapter implements DeviceProtocolSecurityCapabilities, LegacySecurityPropertyConverter {

    @Inject
    public SimplePasswordSecuritySupport(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(propertySpecService, thesaurus);
    }

    protected com.energyict.mdc.upl.security.DeviceProtocolSecurityCapabilities getSecuritySupport() {
        if (securitySupport == null) {
            securitySupport = new com.energyict.protocolimplv2.security.SimplePasswordSecuritySupport(propertySpecService);
        }
        return securitySupport;
    }
}