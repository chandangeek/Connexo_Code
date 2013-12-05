package com.energyict.mdc.protocol;

import com.energyict.mdc.protocol.dynamic.ConnectionProperty;
import com.energyict.mdc.protocol.dynamic.HasDynamicProperties;

import java.util.List;
import java.util.Set;

/**
 * Models a component that will know how to physically
 * setup a connection with a remote device
 * and what properties are required to do that.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-11 (16:16)
 */
public interface ConnectionType extends Pluggable {

    /**
     * Returns if this ConnectionType allows simultaneous
     * connections to be created or not.
     *
     * @return <code>true</code> iff this ConnectionType allows simultaneous connections
     */
    public boolean allowsSimultaneousConnections();

    /**
     * Returns <code>true</code> when this ConnectionType supports
     * the communication window concept.
     *
     * @return A flag that indicates if this ConnectionType supports ComWindows
     */
    public boolean supportsComWindow();

    /**
     * Gets the {@link ComPortType}s that are supported by this ConnectionType.
     *
     * @return The Set of support ComPortType
     */
    public Set<ComPortType> getSupportedComPortTypes();

    /**
     * Establishes a connection with a device from the values
     * specified in the {@link ConnectionProperty ConnectionProperties}.
     *
     * @param properties The ConnectionTaskProperties
     * @return The ComChannel that can be used to communicate with the device
     * @throws ConnectionException Thrown when the connection to the device failed
     */
    public ComChannel connect (List<ConnectionProperty> properties) throws ConnectionException;

    /**
     * Terminates the connection with the device and release resources.
     * E.g.: for modem-based connectionTypes, we should hang up the modem and release the line
     * Note: the implementer should not close the actual {@link ComChannel}, cause this is done elsewhere.
     *
     * @throws ConnectionException Thrown in case of an exception
     */
    public void disconnect(ComChannel comChannel) throws ConnectionException;

}