package com.energyict.mdc.engine.config;

import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;
import com.energyict.mdc.ports.ComPortType;

import java.time.Instant;
import java.util.List;

/**
 * Models a collection of {@link com.energyict.mdc.engine.config.ComPort}s with similar characteristics.
 * One of those characteristics is the nature of the ComPort,
 * i.e. if it is inbound or outbound.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-11 (10:02)
 */
public interface ComPortPool extends HasId, HasName {

    String getName();
    void setName(String name);

    /**
     * Indicates if this ComPortPool is dedicated to inbound communication.
     *
     * @return A flag that indicates if this ComPortPool is dedicated
     *         to inbound communication (<code>true</code>
     *         or outbound communication (<code>false</code>)
     */
    boolean isInbound();

    /**
     * Tests if this ComPortPool is active.
     * Only active ComPortPools can be used by the {@link ComServer}.
     *
     * @return A flag that indicates if this ComPortPool is active (<code>true</code>) or inactive (<code>false</code>).
     */
    boolean isActive();
    void setActive(boolean active);

    /**
     * Gets the description that serves as documentation.
     *
     * @return The description
     */
    String getDescription();
    void setDescription(String description);

    /**
     * Makes this ComPortPool obsolete, i.e. it will appear as it no longer exists
     */
    void makeObsolete();

    /**
     * Indicates if this CompPortPool is marked as deleted
     *
     * @return A flag that indicates if this ComPortPool is marked as deleted
     */
    boolean isObsolete();

    /**
     * Gets the date of this ComPortPool was made obsolete
     *
     * @return The date when this ComPortPool was made obsolete
     *         or <code>null</code> when this ComPortPool is not obsolete at all.
     */
    Instant getObsoleteDate();

    /**
     * Gets the {@link ComPortType type} of {@link com.energyict.mdc.engine.config.ComPort}s
     * that are supported by this OutboundComPortPool.
     *
     * @return The ComPortType
     */
    ComPortType getComPortType();

    List<? extends ComPort> getComPorts();

    void delete();

    long getVersion();
    
    void update();
}