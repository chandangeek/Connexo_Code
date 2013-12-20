package com.energyict.mdc.engine.model;

import com.energyict.mdc.protocol.api.ComPortType;
import java.util.Date;

/**
 * Models a port that can be used to communicate with a device.
 * This will actually map onto a port of a physical machine
 * on which a {@link ComServer} is configured to run.
 * Therefore, a ComPort is said to be dedicated to that
 * particular ComServer.
 * <p/>
 * The types of ports are devided in two main categories that
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
public interface ComPort {

    /**
     * Gets the timestamp of the last modification applied to this ComPort.
     *
     * @return The timestamp of the last modification
     */
    public Date getModificationDate ();

    /**
     * Gets the {@link ComServer} to which this ComPort belongs.
     *
     * @return The ComServer that owns this ComPort
     */
    public ComServer getComServer ();

    public void setComServer(ComServer comServer);

    /**
     * Tests if this ComPort is active.
     * Only active ComPorts can be used by the {@link ComServer}.
     *
     * @return A flag that indicates if this ComPort is active (<code>true</code>) or inactive (<code>false</code>).
     */
    public boolean isActive ();

    /**
     * Sets the active status of this ComPort
     */
    public void setActive(boolean activate);

    /**
     * Gets the description of this ComPort.
     *
     * @return The description
     */
    public String getDescription ();

    /**
     * Indicates if this ComPort is dedicated to inbound communication.
     *
     * @return A flag that indicates if this ComPort is dedicated
     *         to inbound communication (<code>true</code>
     *         or outbound communication (<code>false</code>)
     */
    public boolean isInbound ();

    /**
     * Gets the number of simultaneous connections that this ComPort will allow.
     * ComPort types that do not support simultaneous connections at all
     * will by default return 1.
     *
     * @return The number of simultaneous connections
     */
    public int getNumberOfSimultaneousConnections ();
    public void setNumberOfSimultaneousConnections(int numberOfSimultaneousConnections);

    /**
     * Gets the {@link ComPortType type} of this OutboundComPort.
     *
     * @return The ComPortType
     */
    public ComPortType getComPortType();
    public void setComPortType(ComPortType type);


    public String getType();

    public String getName();
    public void setName(String name);

    public void setModificationDate(Date modificationDate);

    public void setDescription(String description);

    public long getId();

    public void save();

    void delete();

    public interface Builder<B extends Builder<B,C>, C extends ComPort> {
        B name(String name);
        B comPortType(ComPortType comPortType);
        B comServer(ComServer comServer);
        B active(boolean active);
        B description(String description);
        C add();
    }

}