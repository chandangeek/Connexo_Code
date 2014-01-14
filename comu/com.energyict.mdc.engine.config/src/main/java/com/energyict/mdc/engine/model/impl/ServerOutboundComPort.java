package com.energyict.mdc.engine.model.impl;

import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.OutboundComPort;

/**
 * Adds behavior to {@link com.energyict.mdc.engine.model.OutboundComPort} that is private
 * to the server side implementation.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-19 (10:02)
 */
public interface ServerOutboundComPort extends ServerComPort, OutboundComPort {

    void init(ComServer owner);
}