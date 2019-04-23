package com.energyict.protocolimplv2.nta.dsmr40.common;

import com.energyict.dlms.protocolimplv2.SecurityProvider;
import com.energyict.protocolimplv2.nta.dsmr23.common.CryptoDSMR23SecurityProvider;
import com.energyict.protocolimplv2.nta.dsmr40.Dsmr40Properties;
import com.energyict.protocolimplv2.nta.dsmr40.Dsmr40SecurityProvider;


/**
 * Copyrights EnergyICT
 * Date: 6/02/13
 * Time: 15:08
 * Author: khe
 */
public class CryptoDSMR40Properties extends Dsmr40Properties {
    public static final Boolean DEFAULT_CRYPTOSERVER_VALUE = false;

    public boolean useCryptoServer() {
        return getProperties().<Boolean>getTypedProperty(CryptoDSMR23SecurityProvider.CRYPTOSERVER_USAGE, DEFAULT_CRYPTOSERVER_VALUE);
    }

    @Override
    public SecurityProvider getSecurityProvider() {
        if (useCryptoServer()) {
            return new CryptoDSMR40SecurityProvider(getProperties(), getSecurityPropertySet().getAuthenticationDeviceAccessLevel());
        } else {
            return new Dsmr40SecurityProvider(getProperties(), getSecurityPropertySet().getAuthenticationDeviceAccessLevel());
        }
    }
}