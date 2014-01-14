package com.energyict.mdc.engine.model.impl;

import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.ModemBasedInboundComPort;
import com.energyict.mdc.engine.model.ServerComChannelBasedInboundComPort;

/**
 * Adds behavior to {@link com.energyict.mdc.engine.model.ModemBasedInboundComPort} that is private
 * to the server side implementation.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-06-14 (11:14)
 */
public interface ServerModemBasedInboundComPort extends ServerComChannelBasedInboundComPort, ModemBasedInboundComPort {

    void init(ComServer owner);
}