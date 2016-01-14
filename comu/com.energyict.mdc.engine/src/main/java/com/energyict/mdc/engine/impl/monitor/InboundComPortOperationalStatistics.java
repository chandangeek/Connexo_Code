package com.energyict.mdc.engine.impl.monitor;

import com.energyict.mdc.engine.monitor.OperationalStatistics;

import java.time.Instant;

/**
 * Models the operational statistics that are gathered by the process
 * that monitors a ComPortListener.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-03 (10:50)
 */
public interface InboundComPortOperationalStatistics extends OperationalStatistics {

    /**
     * Gets the total number of connections
     * from inbound devices to the ComPortListener.
     *
     * @return The total number of connections
     */
     long getNumberOfConnections ();

    /**
     * Gets the timestamp of the last time
     * a connection from a device to the
     * ComPortListener
     * was established.
     *
     * @return The timestamp of last activity of every thread
     */
     Instant getLastConnectionTimestamp ();

    /**
     * Gets the unique identifier of the last device
     * that established a connection to the
     * ComPortListener.
     *
     * @return The id of the last device that established a connection
     */
     String getLastConnectionDeviceId ();

}