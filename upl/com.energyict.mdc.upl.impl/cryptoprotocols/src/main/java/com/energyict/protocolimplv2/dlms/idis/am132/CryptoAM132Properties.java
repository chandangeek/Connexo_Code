package com.energyict.protocolimplv2.dlms.idis.am132;

import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.protocolimplv2.SecurityProvider;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimplv2.dlms.idis.am132.properties.AM132Properties;
import com.energyict.protocolimplv2.dlms.idis.am540.CryptoAM540SecurityProvider;

/**
 * Created by cisac on 6/6/2017.
 */
public class CryptoAM132Properties extends AM132Properties {

    public CryptoAM132Properties(PropertySpecService propertySpecService, NlsService nlsService) {
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
