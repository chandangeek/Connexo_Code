package com.energyict.mdc.io;

import com.energyict.mdc.ports.ComPortType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.upl.properties.HasDynamicProperties;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.protocol.exceptions.ConnectionException;

import java.util.Set;

/**
 * Models a component that will know how to physically
 * setup a connection with a remote device
 * and what properties are required to do that.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-11 (16:16)
 */
public interface ConnectionType extends HasDynamicProperties{

    enum Property {

        /**
         * Provides the OS name of the (serial) Communication port to use
         */
        COMP_PORT_NAME("ComPortName");

        private final String name;

        Property(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }
    }


    /**
     * Defines the direction of the ConnectionType.
     * <ul>
     * <li>Outbound means that the Collection system will use the ConnectionType to contact the device.
     * This means that some properties will be required in order to uniquely contact a Device.</li>
     * <li>Inbound means that the Device itself will contact the Collection system.
     * Most of the settings will be done in the Device and not on the ConnectionType.
     * (This does not mean that we cannot provide a list of properties)</li>
     * </ul>
     */
    enum ConnectionTypeDirection {
        OUTBOUND,
        INBOUND,

        /**
         * The purpose of NULL is solely that the direction is not defined,
         * it should not be used as an implementation type
         */
        NULL;

        public static ConnectionTypeDirection fromString(String direction) {
            for (ConnectionTypeDirection connectionTypeDirection : ConnectionTypeDirection.values()) {
                if (connectionTypeDirection.name().equalsIgnoreCase(direction)) {
                    return connectionTypeDirection;
                }
            }
            return NULL;
        }
    }

    /**
     * Establishes a connection with a device using the property values
     * that were injected with {@link HasDynamicProperties#setUPLProperties(TypedProperties)}.
     *
     * @return The ComChannel that can be used to communicate with the device
     * @throws ConnectionException Thrown when the connection to the device failed
     */
    ComChannel connect() throws ConnectionException;

    /**
     * Terminates the connection with the device and release resources.
     * E.g.: for modem-based connectionTypes, we should hang up the modem and release the line
     * Note: the implementer should not close the actual {@link ComChannel}, cause this is done elsewhere.
     *
     * @throws ConnectionException Thrown in case of an exception
     */
    void disconnect(ComChannel comChannel) throws ConnectionException;

    /**
     * Returns if this ConnectionType allows simultaneous
     * connections to be created or not.
     *
     * @return <code>true</code> iff this ConnectionType allows simultaneous connections
     */
    boolean allowsSimultaneousConnections();

    /**
     * Returns <code>true</code> when this ConnectionType supports
     * the communication window concept.
     * If this is NOT the case, then ConnectionTasks that
     * use this ConnectionType are NOT required to have a ComWindow.
     *
     * @return A flag that indicates if this ConnectionType supports ComWindows
     */
    boolean supportsComWindow();

    /**
     * Gets the {@link ComPortType}s that are supported by this ConnectionType.
     * This will limit the choise of ComPortPools
     * when a ConnectionType is linked to a ConnectionTask,
     * i.e. you will only be able to link the ConnectionTask to OutboundComPortPool
     * that are compatible.
     *
     * @return The
     */
    Set<ComPortType> getSupportedComPortTypes();

    /**
     * Provides meta information for the Collection system to inform whether this ConnectionType
     * can be used for Inbound communication or for Outbound communication
     *
     * @return the direction of the ConnectionType
     */
    ConnectionTypeDirection getDirection();

    /**
     * Returns the implementation version.
     *
     * @return a version string
     */
    String getVersion();

}
