package com.energyict.protocolimplv2.securitysupport;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.security.LegacySecurityPropertyConverter;

import javax.inject.Inject;

/**
 * Provides general security <b>capabilities</b> for an Ansi C12 protocol
 * <p>
 *
 * Date: 21/01/13
 * Time: 12:02
 */
public class AnsiC12SecuritySupport extends AbstractSecuritySupportAdapter implements DeviceProtocolSecurityCapabilities, LegacySecurityPropertyConverter {

    @Inject
    public AnsiC12SecuritySupport(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(propertySpecService, thesaurus);
    }

    protected com.energyict.mdc.upl.security.DeviceProtocolSecurityCapabilities getSecuritySupport() {
        if (securitySupport == null) {
            securitySupport = new com.energyict.protocolimplv2.security.AnsiC12SecuritySupport(propertySpecService);
        }
        return securitySupport;
    }
}