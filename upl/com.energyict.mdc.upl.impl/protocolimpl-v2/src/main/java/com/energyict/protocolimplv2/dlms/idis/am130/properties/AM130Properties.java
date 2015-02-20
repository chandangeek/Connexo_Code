package com.energyict.protocolimplv2.dlms.idis.am130.properties;

import com.energyict.dlms.protocolimplv2.SecurityProvider;
import com.energyict.protocolimplv2.dlms.idis.am500.properties.IDISProperties;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 12/02/2015 - 9:18
 */
public class AM130Properties extends IDISProperties {

    @Override
    public SecurityProvider getSecurityProvider() {
        if (securityProvider == null) {
            securityProvider = new DSMR40SecurityProvider(getProperties(), getSecurityPropertySet().getAuthenticationDeviceAccessLevel());
        }
        return securityProvider;
    }
}