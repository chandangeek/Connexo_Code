package com.energyict.protocolimplv2.nta.dsmr23.common;

import com.energyict.dlms.protocolimplv2.SecurityProvider;
import com.energyict.protocolimplv2.nta.abstractnta.NTASecurityProvider;
import com.energyict.protocolimplv2.nta.dsmr23.Dsmr23Properties;
import com.energyict.protocolimplv2.nta.esmr50.common.ESMR50SecurityProvider;


/**
 * Copyrights EnergyICT
 * Date: 6/02/13
 * Time: 15:08
 * Author: khe
 */
public class CryptoDSMR23Properties extends Dsmr23Properties {
    public static final Boolean DEFAULT_CRYPTOSERVER_VALUE = false;

    public boolean useCryptoServer() {
        return getProperties().<Boolean>getTypedProperty(CryptoDSMR23SecurityProvider.CRYPTOSERVER_USAGE, DEFAULT_CRYPTOSERVER_VALUE);
    }

    @Override
    public SecurityProvider getSecurityProvider() {
        boolean replayAttackPrevention = replayAttackPreventionEnabled();
        if (useCryptoServer()) {
            this.securityProvider = new CryptoDSMR23SecurityProviderReplayAttack(getProperties(), getSecurityPropertySet().getAuthenticationDeviceAccessLevel(), replayAttackPrevention);
        } else {
            if(replayAttackPrevention){
                this.securityProvider = new ESMR50SecurityProvider(getProperties(), getSecurityPropertySet().getAuthenticationDeviceAccessLevel());
            } else {
                this.securityProvider = new NTASecurityProvider(getProperties(), getSecurityPropertySet().getAuthenticationDeviceAccessLevel());
            }

        }
        return this.securityProvider;
    }
}