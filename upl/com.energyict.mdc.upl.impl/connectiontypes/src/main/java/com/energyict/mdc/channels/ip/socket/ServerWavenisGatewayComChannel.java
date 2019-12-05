package com.energyict.mdc.channels.ip.socket;

import com.energyict.concentrator.communication.driver.rf.eictwavenis.WavenisStack;
import com.energyict.mdc.protocol.ComChannelRemoteAddress;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 8/06/2015 - 15:05
 */
public interface ServerWavenisGatewayComChannel {

    /**
     * Stack instance that represents the interface to the Wavecard of the MUC
     */
    public WavenisStack getWavenisStack();

}