/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.config;

/**
 * Models an {@link InboundComPort} that is using IP based infrastructure.
 * These types of ports are typically configured to listen on an IP port
 * for incomming connections.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-03-27 (17:08)
 */
public interface IPBasedInboundComPort extends InboundComPort {

    /**
     * Gets the IP port number on which this ComPort will
     * listen for incoming connections.
     *
     * @return The IP port number
     */
    public int getPortNumber();
    public void setPortNumber(int portNumber);

    interface IpBasedInboundComPortBuilder<B extends IpBasedInboundComPortBuilder<B,C>, C extends IPBasedInboundComPort>
            extends InboundComPortBuilder<B, C>{
        // There used to be methods here, but alas
    }

}