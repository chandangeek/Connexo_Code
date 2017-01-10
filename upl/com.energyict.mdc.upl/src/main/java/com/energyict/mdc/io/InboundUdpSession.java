package com.energyict.mdc.io;

import com.energyict.mdc.protocol.ComChannel;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-01-10 (14:11)
 */
public interface InboundUdpSession extends VirtualUdpSession {
    /**
     * Properly waits for an initial UDP packet and create an input- and outputStream for them.
     *
     * @return a DatagramComChannel modeled by the initial UDP packet.
     */
    ComChannel accept();
}