package com.energyict.mdc.engine.config;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;
import com.energyict.mdc.ports.ComPortType;

import java.time.Instant;

/**
 * Models a port that can be used to communicate with a device.
 * This will actually map onto a port of a physical machine
 * on which a {@link ComServer} is configured to run.
 * Therefore, a ComPort is said to be dedicated to that
 * particular ComServer.
 * <p/>
 * The types of ports are split in two main categories that
 * relate to the in or outbound nature of the communication.
 * Outbound communication is initiated by the ComServer
 * while inbound communication is initiated by the device.
 * An inbound communication port will therefore wait for
 * connection attempts against the port from a device.
 * Outbound communication ports will sit in a {@link ComPortPool pool}
 * waiting for the communication server to start communications
 * with a device. At that time, the ComPort will be in use.
 * <p/>
 * Several types of inbound communications are supported:
 * <ul>
 * <li>Modem based</li>
 * <li>IP based</li>
 * <li>Servlet based</li>
 * </ul>
 * Specific subclasses will focus on the attributes that
 * relate to each of the types mentioned above.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-03-27 (16:26)
 */
@ProviderType
public interface ComPort extends HasId, HasName  {

    /**
     * Gets the timestamp of the last modification applied to this ComPort.
     *
     * @return The timestamp of the last modification
     */
    Instant getModificationDate();

    /**
     * Gets the {@link ComServer} to which this ComPort belongs.
     *
     * @return The ComServer that owns this ComPort
     */
    ComServer getComServer ();

    /**
     * Tests if this ComPort is active.
     * Only active ComPorts can be used by the {@link ComServer}.
     *
     * @return A flag that indicates if this ComPort is active (<code>true</code>) or inactive (<code>false</code>).
     */
    boolean isActive ();

    /**
     * Sets the active status of this ComPort.
     */
    void setActive(boolean activate);

    /**
     * Gets the description of this ComPort.
     *
     * @return The description
     */
    String getDescription ();

    /**
     * Indicates if this ComPort is dedicated to inbound communication.
     *
     * @return A flag that indicates if this ComPort is dedicated
     *         to inbound communication (<code>true</code>
     *         or outbound communication (<code>false</code>)
     */
    boolean isInbound ();

    /**
     * Gets the number of simultaneous connections that this ComPort will allow.
     * ComPort types that do not support simultaneous connections at all
     * will by default return 1.
     *
     * @return The number of simultaneous connections
     */
    int getNumberOfSimultaneousConnections ();
    void setNumberOfSimultaneousConnections(int numberOfSimultaneousConnections);

    /**
     * Gets the {@link ComPortType type} of this OutboundComPort.
     *
     * @return The ComPortType
     */
    ComPortType getComPortType();
    void setComPortType(ComPortType type);


    String getName();
    void setName(String name);

    void setDescription(String description);

    void update();

    interface Builder<B extends Builder<B,C>, C extends ComPort> {
        B active(boolean active);
        B description(String description);
        C add();
    }

    /**
     * Makes this ComPort obsolete, i.e. it will no longer be available
     * to be used to connect to devices nor will it be returned by
     * the service finder methods.
     * This will also remove the ComPort from all ComPortPools
     * it belongs to.
     *
     */
    void makeObsolete ();

    /**
     * Indicates if this ComPort is obsolete.
     *
     * @return A flag that indicates if this ComPort is obsolete
     */
    boolean isObsolete ();

    /**
     * Gets the date on which this ComPort was made obsolete.
     *
     * @return The date when this ComPort was made obsolete
     *         or <code>null</code> when this ComPort is not obsolete at all.
     */
    Instant getObsoleteDate();

    long getVersion();
}