package com.energyict.mdc.engine.model;

import com.energyict.mdc.protocol.api.ComPortType;
import java.util.Date;

/**
 * Models a collection of {@link com.energyict.mdc.engine.model.ComPort}s with similar characteristics.
 * One of those characteristics is the nature of the ComPort,
 * i.e. if it is inbound or outbound.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-11 (10:02)
 */
public interface ComPortPool {

    public long getId();

    public String getName();
    public void setName(String name);

    /**
     * Indicates if this ComPortPool is dedicated to inbound communication.
     *
     * @return A flag that indicates if this ComPortPool is dedicated
     *         to inbound communication (<code>true</code>
     *         or outbound communication (<code>false</code>)
     */
    public boolean isInbound();

    /**
     * Tests if this ComPortPool is active.
     * Only active ComPortPools can be used by the {@link ComServer}.
     *
     * @return A flag that indicates if this ComPortPool is active (<code>true</code>) or inactive (<code>false</code>).
     */
    public boolean isActive();
    public void setActive(boolean active);

    /**
     * Gets the description that serves as documentation.
     *
     * @return The description
     */
    public String getDescription();
    public void setDescription(String description);

    /**
     * Makes this ComPortPool obsolete, i.e. it will appear as it no longer exists
     */
    public void makeObsolete();

    /**
     * Indicates if this CompPortPool is marked as deleted
     *
     * @return A flag that indicates if this ComPortPool is marked as deleted
     */
    public boolean isObsolete();

    /**
     * Gets the date of this ComPortPool was made obsolete
     *
     * @return The date when this ComPortPool was made obsolete
     *         or <code>null</code> when this ComPortPool is not obsolete at all.
     */
    public Date getObsoleteDate();

    /**
     * Gets the {@link ComPortType type} of {@link com.energyict.mdc.engine.model.ComPort}s
     * that are supported by this OutboundComPortPool.
     *
     * @return The ComPortType
     */
    public ComPortType getComPortType();
    public void setComPortType(ComPortType comPortType);

    public void save();

}