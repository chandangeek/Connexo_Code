package com.energyict.mdc.engine.events;

import aQute.bnd.annotation.ProviderType;

/**
 * Models an event that relates to communication that occurs
 * on an established connection with a {@link com.energyict.mdc.protocol.api.device.BaseDevice device}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-31 (14:34)
 */
@ProviderType
public interface CommunicationEvent extends ConnectionEvent {

    /**
     * Returns <code>true</code> iff this events indicates
     * that data was read from a connection with a {@link com.energyict.mdc.protocol.api.device.BaseDevice device}.
     *
     * @return <code>true</code> iff this events indicates that data was read
     */
    public boolean isRead ();

    /**
     * Returns <code>true</code> iff this events indicates
     * that data was written to a connection with a {@link com.energyict.mdc.protocol.api.device.BaseDevice device}.
     *
     * @return <code>true</code> iff this events indicates that data was written
     */
    public boolean isWrite ();

    /**
     * Gets the bytes that were read/written from or to
     * a connection with a {@link com.energyict.mdc.protocol.api.device.BaseDevice device}.
     *
     * @return The bytes that were read or written
     */
    public byte[] getBytes ();

}