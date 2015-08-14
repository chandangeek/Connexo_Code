package com.energyict.protocolimplv2.dlms.idis.am130.properties;

import com.energyict.cpo.TypedProperties;
import com.energyict.protocolimplv2.nta.abstractnta.NTASecurityProvider;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.DSMR40RespondingFrameCounterHandler;

/**
 * Extension of the normal NTA security provider, adding a limitation to the framecounter.
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 12/02/2015 - 10:14
 */
public class IDISSecurityProvider extends NTASecurityProvider {

    /**
     * Create a new instance of NTASecurityProvider
     *
     * @param properties          - contains the keys for the authentication/encryption
     * @param authenticationLevel
     */
    public IDISSecurityProvider(TypedProperties properties, int authenticationLevel) {
        super(properties, authenticationLevel);
        setRespondingFrameCounterHandling(new DSMR40RespondingFrameCounterHandler());
    }
}