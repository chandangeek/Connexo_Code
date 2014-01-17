package com.energyict.mdc.engine.model.impl;

import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.ServerComChannelBasedInboundComPort;
import com.energyict.mdc.engine.model.UDPBasedInboundComPort;

/**
 * Adds behavior to {@link com.energyict.mdc.engine.model.UDPBasedInboundComPort} that is private
 * to the server side implementation.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-06-14 (11:14)
 */
public interface ServerUDPBasedInboundComPort extends ServerIPBasedInboundComPort, UDPBasedInboundComPort {

}