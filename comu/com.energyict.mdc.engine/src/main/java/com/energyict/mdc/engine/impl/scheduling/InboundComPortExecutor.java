package com.energyict.mdc.engine.impl.scheduling;

import com.energyict.mdc.protocol.api.ComChannel;

/**
 * Performs the execution of an inbound call
 * <p/>
 * Copyrights EnergyICT
 * Date: 22/10/12
 * Time: 11:12
 */
public interface InboundComPortExecutor {

    /**
     * Handles the inbound call
     *
     * @param comChannel the ComChannel the inbound call has set up
     */
    public void execute(ComChannel comChannel);

}
