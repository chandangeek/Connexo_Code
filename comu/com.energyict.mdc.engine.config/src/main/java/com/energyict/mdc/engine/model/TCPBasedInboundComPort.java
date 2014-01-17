package com.energyict.mdc.engine.model;

import com.energyict.mdc.engine.model.impl.ServerTCPBasedInboundComPort;

/**
 * Models an {@link IPBasedInboundComPort} that is using TCP/IP based infrastructure.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-02 (11:18)
 */
public interface TCPBasedInboundComPort extends IPBasedInboundComPort {

    interface TCPBasedInboundComPortBuilder extends IpBasedInboundComPortBuilder<TCPBasedInboundComPortBuilder, ServerTCPBasedInboundComPort> {

    }

}