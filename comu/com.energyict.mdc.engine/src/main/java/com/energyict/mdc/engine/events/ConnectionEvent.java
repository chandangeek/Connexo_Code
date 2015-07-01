package com.energyict.mdc.engine.events;

import aQute.bnd.annotation.ProviderType;

/**
 * Models an event that relates to physical connections with a device.
 * There is no difference between inbound or outbound connections.
 * Every type of connection that is being established or closed
 * will result in a ConnectionEvent.
 * Failures to establish a connection will however, always be for
 * outbound connections because inbound connections are out of
 * the ComServer's control and the ComServer will never find out
 * when a remote device failed to establish an inbound connection.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-30 (17:50)
 */
@ProviderType
public interface ConnectionEvent extends ComServerEvent, DeviceRelatedEvent, ConnectionTaskRelatedEvent, ComPortRelatedEvent {

    /**
     * Returns <code>true</code> iff this events was
     * emitted as a result of a physical connection
     * being established.
     *
     * @return <code>true</code> iff the physical connection was established
     */
    public boolean isEstablishing ();

    /**
     * Returns <code>true</code> iff this event was
     * emitted after a failure to establish a physical
     * connection with a device.
     *
     * @return <code>true</code> iff this event indicates a failure to establish a connection
     */
    public boolean isFailure ();

    /**
     * Returns a message that describes the failure to connect
     * to a physcial device. This obviously only returns
     * such a message iff this event is an indication
     * for a connection failure, i.e. {@link #isFailure()} return <code>true</code>.
     *
     * @return The message that describes the failure
     */
    public String getFailureMessage ();

    /**
     * Returns <code>true</code> iff this events was
     * emitted as a result of a physical connection
     * being closed.
     *
     * @return <code>true</code> iff the physical connection was closed
     */
    public boolean isClosed ();

}