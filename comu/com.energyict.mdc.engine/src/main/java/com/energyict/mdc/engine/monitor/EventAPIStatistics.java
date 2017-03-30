/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.monitor;

import aQute.bnd.annotation.ProviderType;

/**
 * Models the statistics of the event API that are gathered by the process
 * that monitors a RunningComServer.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-03 (11:09)
 */
@ProviderType
public interface EventAPIStatistics {

    /**
     * Gets the number of clients that are connected to the
     * RunningComServer's
     * event api to receive event notifications.
     *
     * @return The number of event API clients
     */
     int getNumberOfClients ();

    /**
     * Increments the number of clients that have registered
     * in interest to receive events as they are being published.
     */
    void clientRegistered ();

    /**
     * Decrements the number of clients that have registered
     * in interest to receive events as they are being published.
     */
    void clientUnregistered ();

    /**
     * Gets the number of events that have been collected by the
     * RunningComServer.
     *
     * @return The number of events that have been collected
     */
    long getNumberOfEvents ();

}