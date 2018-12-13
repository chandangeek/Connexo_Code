/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core.inbound;

import com.energyict.mdc.engine.impl.core.ComPortRelatedComChannel;

public interface InboundComPortConnector extends AutoCloseable {

    /**
     * Listen, wait and accept an incoming Call, and provide a decent {@link ComPortRelatedComChannel} in return. <br/>
     * An accept on a specific channel (Socket, DatagramSocket, Serial, ...) will most likely be a blocking call.
     * The blocking call can be interrupted by any Exception. To make it manageable for the ComServer,
     * wrap all the connection related exceptions into an InboundCommunicationException.<br/>
     * Never return a null, but always try to use a descent ComChannel (see VoidComChannel for dummy implementations)<br/>
     * If for some reason you should configure a timeOut on your accept, then it is advised to the set the
     * timeOut equal to the {@link com.energyict.mdc.engine.config.ComServer#getChangesInterPollDelay()}
     *
     * @return a ComChannel based on the received call.
     */
    ComPortRelatedComChannel accept();

}