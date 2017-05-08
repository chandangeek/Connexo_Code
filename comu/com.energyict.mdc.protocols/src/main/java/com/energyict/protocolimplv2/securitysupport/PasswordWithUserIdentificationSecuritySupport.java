package com.energyict.protocolimplv2.securitysupport;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.security.LegacySecurityPropertyConverter;

import javax.inject.Inject;

/**
 * Provides general security <b>capabilities</b> for DeviceProtocols
 * that use a single password and a UserIdentification to do authentication/encryption.<br/>
 * Be aware that the UserIdentification is validated as a string, but can also just
 * contain a numerical value.
 * <p>
 *
 * Date: 14/01/13
 * Time: 9:28
 */
public class PasswordWithUserIdentificationSecuritySupport extends AbstractSecuritySupportAdapter implements DeviceProtocolSecurityCapabilities, LegacySecurityPropertyConverter {

    @Inject
    public PasswordWithUserIdentificationSecuritySupport(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(propertySpecService, thesaurus);
    }

    protected com.energyict.mdc.upl.security.DeviceProtocolSecurityCapabilities getSecuritySupport() {
        if (securitySupport == null) {
            securitySupport = new com.energyict.protocolimplv2.security.PasswordWithUserIdentificationSecuritySupport(propertySpecService);
        }
        return securitySupport;
    }
}