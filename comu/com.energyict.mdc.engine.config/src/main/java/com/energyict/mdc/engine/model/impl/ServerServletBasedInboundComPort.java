package com.energyict.mdc.engine.model.impl;

import com.energyict.mdc.engine.model.ComServer;

/**
 * Adds behavior to {@link ServletBasedInboundComPort} that is private
 * to the server side implementation.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-11 (11:34)
 */
public interface ServerServletBasedInboundComPort extends ServerInboundComPort, ServletBasedInboundComPort {

    public void init(ComServer owner);
}