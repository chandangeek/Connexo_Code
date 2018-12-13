package com.energyict.protocolimplv2.securitysupport;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.security.LegacySecurityPropertyConverter;

import javax.inject.Inject;

/**
 * Provides general security <b>capabilities</b> for a DLMS protocol, which
 * has a SecurityObject for each possible client.
 * <p>
 *
 * Date: 18/06/13
 * Time: 15:02
 */
public class DlmsSecuritySupportPerClient extends AbstractSecuritySupportAdapter implements DeviceProtocolSecurityCapabilities, LegacySecurityPropertyConverter {

    @Inject
    public DlmsSecuritySupportPerClient(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(propertySpecService, thesaurus);
    }

    protected com.energyict.mdc.upl.security.DeviceProtocolSecurityCapabilities getSecuritySupport() {
        if (securitySupport == null) {
            securitySupport = new com.energyict.protocolimplv2.security.DlmsSecuritySupportPerClient(propertySpecService);
        }
        return securitySupport;
    }
}