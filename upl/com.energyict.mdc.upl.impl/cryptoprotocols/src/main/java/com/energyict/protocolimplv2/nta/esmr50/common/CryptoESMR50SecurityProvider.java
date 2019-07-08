package com.energyict.protocolimplv2.nta.esmr50.common;

import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.protocolimplv2.nta.dsmr23.common.CryptoDSMR23SecurityProvider;
import com.energyict.protocolimplv2.nta.dsmr40.DSMR40RespondingFrameCounterHandler;
import com.energyict.protocolimplv2.nta.dsmr40.common.CryptoDSMR40SecurityProvider;

import java.util.Random;

public class CryptoESMR50SecurityProvider extends CryptoDSMR40SecurityProvider {
    /**
     * Create a new instance of NTASecurityProvider
     *
     * @param properties - contains the keys for the authentication/encryption
     * @param authenticationLevel
     */
    public CryptoESMR50SecurityProvider(TypedProperties properties, int authenticationLevel) {
        super(properties, authenticationLevel);
        setRespondingFrameCounterHandling(new DSMR40RespondingFrameCounterHandler());
    }

    @Override
    public long initializeFrameCounter() {
        return 0;
    }
}
