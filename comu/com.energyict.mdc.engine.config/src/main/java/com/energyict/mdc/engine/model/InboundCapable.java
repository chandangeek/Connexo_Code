package com.energyict.mdc.engine.model;

import com.energyict.mdc.ports.InboundComPort;

import java.util.List;

/**
 * Models the behavior of a component that is capable of accepting inbound connections.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-06-14 (08:58)
 */
public interface InboundCapable {

    /**
     * Gets the {@link InboundComPort}s that are
     * will support the inbound connections.
     *
     * @return The list of InboundComPorts
     */
    public List<InboundComPort> getInboundComPorts ();

}