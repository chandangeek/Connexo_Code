package com.energyict.mdc.engine.model.impl;

import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.ServerComChannelBasedInboundComPort;
import com.energyict.mdc.engine.model.TCPBasedInboundComPort;

/**
 * Adds behavior to {@link com.energyict.mdc.engine.model.TCPBasedInboundComPort} that is private
 * to the server side implementation.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-06-14 (11:14)
 */
public interface ServerTCPBasedInboundComPort extends ServerIPBasedInboundComPort, TCPBasedInboundComPort {

    public void init(ComServer owner);
}