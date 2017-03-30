/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.monitor;

import aQute.bnd.annotation.ProviderType;

import java.util.Date;
import java.util.Optional;

/**
 * Models the operational statistics that are gathered by the process
 * that monitors a ComPortListener.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-03 (10:50)
 */
@ProviderType
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
     Optional<Date> getLastConnectionTimestamp ();

    /**
     * Gets the unique identifier of the last device
     * that established a connection to the
     * ComPortListener.
     *
     * @return The MRId of the last device that established a connection
     */
     String getLastConnectionDeviceMRID ();

}