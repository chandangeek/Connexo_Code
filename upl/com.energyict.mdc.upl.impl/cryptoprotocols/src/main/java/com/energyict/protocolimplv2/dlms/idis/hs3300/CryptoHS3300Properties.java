package com.energyict.protocolimplv2.dlms.idis.hs3300;

import com.energyict.dlms.protocolimplv2.SecurityProvider;
import com.energyict.mdc.upl.messages.legacy.CertificateWrapperExtractor;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimplv2.dlms.idis.hs3300.properties.HS3300Properties;

public class CryptoHS3300Properties extends HS3300Properties {

    public CryptoHS3300Properties(PropertySpecService propertySpecService, NlsService nlsService, CertificateWrapperExtractor certificateWrapperExtractor) {
        super(propertySpecService, nlsService, certificateWrapperExtractor);
    }

    @Override
    public SecurityProvider getSecurityProvider() {
        if (securityProvider == null) {
            securityProvider = new CryptoHS3300SecurityProvider(
                    getProperties(), getSecurityPropertySet().getAuthenticationDeviceAccessLevel(),
                    getSecuritySuite(), getCertificateWrapperExtractor()
            );
        }
        return securityProvider;
    }

}
