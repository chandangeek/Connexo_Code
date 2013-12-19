package com.energyict.mdc.engine.model.impl;

/**
 * Provides an implementation for the {@link com.energyict.mdc.engine.model.TCPBasedInboundComPort} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-02 (13:30)
 */
public class TCPBasedInboundComPortImpl extends IPBasedInboundComPortImpl implements ServerTCPBasedInboundComPort {

    protected TCPBasedInboundComPortImpl () {
        super();
    }

    @Override
    public boolean isTCPBased() {
        return true;
    }

}