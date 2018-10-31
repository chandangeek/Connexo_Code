package com.energyict.protocolimplv2.eict.rtu3.beacon3100;

import com.energyict.dlms.protocolimplv2.SecurityProvider;
import com.energyict.mdc.upl.messages.legacy.CertificateWrapperExtractor;
import com.energyict.protocolimplv2.eict.rtu3.beacon3100.properties.Beacon3100Properties;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 2/11/2016 - 16:48
 */
public class CryptoBeacon3100Properties extends Beacon3100Properties {

    public CryptoBeacon3100Properties(CertificateWrapperExtractor certificateWrapperExtractor) {
        super(certificateWrapperExtractor);
    }

    @Override
    public SecurityProvider getSecurityProvider() {
        if (securityProvider == null) {
            securityProvider = new CryptoBeacon3100SecurityProvider(getProperties(), getSecurityPropertySet().getAuthenticationDeviceAccessLevel(), getSecuritySuite(), getCertificateWrapperExtractor());
        }
        return securityProvider;
    }

}