package com.energyict.mdc.io;

import aQute.bnd.annotation.ProviderType;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-11-04 (10:18)
 */
@ProviderType
public interface InboundUdpSession extends VirtualUdpSession {

    /**
     * Properly waits for an initial UDP packet and create an input- and outputStream for them.
     *
     * @return a DatagramComChannel modeled by the initial UDP packet.
     */
    public ComChannel accept();

}