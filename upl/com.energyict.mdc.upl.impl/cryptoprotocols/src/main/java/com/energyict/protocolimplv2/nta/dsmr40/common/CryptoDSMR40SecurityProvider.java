package com.energyict.protocolimplv2.nta.dsmr40.common;

import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.protocolimplv2.nta.dsmr23.common.CryptoDSMR23SecurityProvider;
import com.energyict.protocolimplv2.nta.dsmr40.DSMR40RespondingFrameCounterHandler;

import java.util.Random;

public class CryptoDSMR40SecurityProvider extends CryptoDSMR23SecurityProvider {
    /**
     * Create a new instance of NTASecurityProvider
     *
     * @param properties - contains the keys for the authentication/encryption
     * @param authenticationLevel
     */
    public CryptoDSMR40SecurityProvider(TypedProperties properties, int authenticationLevel) {
        super(properties, authenticationLevel);
        setRespondingFrameCounterHandling(new DSMR40RespondingFrameCounterHandler());
    }
}
