package com.energyict.protocolimplv2.nta.dsmr50.elster.am540;

import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.dlms.DLMSConnectionException;
import com.energyict.protocolimpl.dlms.g3.G3RespondingFrameCounterHandler;
import com.energyict.protocolimplv2.nta.abstractnta.NTASecurityProvider;

/**
 * Copyrights EnergyICT
 * <p/>
 * Extension of the NTA security provider, using the G3 PLC frame counter handler.
 *
 * @author khe
 * @since 11/04/2016 - 14:04
 */
public class Dsmr50SecurityProvider extends NTASecurityProvider {

    /**
     * Create a new instance of NTASecurityProvider
     *
     * @param properties - contains the keys for the authentication/encryption
     */
    public Dsmr50SecurityProvider(TypedProperties properties, int authenticationLevel) {
        super(properties, authenticationLevel);
        setRespondingFrameCounterHandling(new G3RespondingFrameCounterHandler(DLMSConnectionException.REASON_CONTINUE_INVALID_FRAMECOUNTER));
    }

}