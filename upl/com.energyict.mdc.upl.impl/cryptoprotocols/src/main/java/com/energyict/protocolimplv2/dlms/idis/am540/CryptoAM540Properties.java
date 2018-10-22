package com.energyict.protocolimplv2.dlms.idis.am540;


import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.protocolimplv2.SecurityProvider;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimplv2.dlms.idis.am540.properties.AM540Properties;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 10/10/2016 - 14:01
 */
public class CryptoAM540Properties extends AM540Properties {

    public CryptoAM540Properties(PropertySpecService propertySpecService, NlsService nlsService) {
        super(propertySpecService, nlsService);
    }

    @Override
    public SecurityProvider getSecurityProvider() {
        if (securityProvider == null) {
            securityProvider = new CryptoAM540SecurityProvider(getProperties(), getSecurityPropertySet().getAuthenticationDeviceAccessLevel(), DLMSConnectionException.REASON_CONTINUE_INVALID_FRAMECOUNTER);
        }
        return securityProvider;
    }

}