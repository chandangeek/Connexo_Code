package com.energyict.protocolimplv2.nta.dsmr23.common;

import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.dlms.aso.framecounter.DefaultRespondingFrameCounterHandler;
import com.energyict.protocolimplv2.nta.dsmr40.DSMR40RespondingFrameCounterHandler;

public class CryptoDSMR23SecurityProviderReplayAttack extends CryptoDSMR23SecurityProvider {

    private boolean replayAttackPrevention = false;

    /**
     * Create a new instance of LocalSecurityProvider
     *
     * @param properties - contains the keys for the authentication/encryption
     * @param authenticationLevel
     * @param replayAttackPrevention - replay attack prevention on/off
     */
    public CryptoDSMR23SecurityProviderReplayAttack(TypedProperties properties, int authenticationLevel, boolean replayAttackPrevention) {
        super(properties, authenticationLevel);
        this.replayAttackPrevention = replayAttackPrevention;
        initializeRespondingFrameCounterHandler();
    }

    protected void initializeRespondingFrameCounterHandler() {
        if(replayAttackPrevention){
            setRespondingFrameCounterHandling(new DSMR40RespondingFrameCounterHandler());
        } else {
            setRespondingFrameCounterHandling(new DefaultRespondingFrameCounterHandler());
        }

    }
}
