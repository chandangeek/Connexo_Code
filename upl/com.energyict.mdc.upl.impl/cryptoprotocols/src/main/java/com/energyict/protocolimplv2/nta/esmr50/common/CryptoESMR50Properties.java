package com.energyict.protocolimplv2.nta.esmr50.common;

import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.dlms.protocolimplv2.SecurityProvider;
import com.energyict.protocolimplv2.nta.dsmr23.common.CryptoDSMR23SecurityProvider;
import com.energyict.protocolimplv2.nta.dsmr40.common.CryptoDSMR40SecurityProvider;

public class CryptoESMR50Properties extends ESMR50Properties {
    public static final Boolean DEFAULT_CRYPTOSERVER_VALUE = false;

    public CryptoESMR50Properties(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public SecurityProvider getSecurityProvider() {
        if (useCryptoServer()) {
            return new CryptoDSMR40SecurityProvider(getProperties(), getSecurityPropertySet().getAuthenticationDeviceAccessLevel());
        } else {
            return new ESMR50SecurityProvider(getProperties(), getSecurityPropertySet().getAuthenticationDeviceAccessLevel());
        }
    }

    public boolean useCryptoServer() {
        return getProperties().<Boolean>getTypedProperty(CryptoDSMR23SecurityProvider.CRYPTOSERVER_USAGE, DEFAULT_CRYPTOSERVER_VALUE);
    }

}
