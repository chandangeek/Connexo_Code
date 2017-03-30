/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.monitor;

import aQute.bnd.annotation.ProviderType;

/**
 * Models the statistics of the query API that are gathered by the process
 * that monitors a RunningComServer.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-03 (11:09)
 */
@ProviderType
public interface QueryAPIStatistics {

    /**
     * Gets the number of clients that are connected to the
     * RunningComServer's
     * query api to execute queries.
     *
     * @return The number of event API clients
     */
     int getNumberOfClients ();

    /**
     * Gets the number of failures that have occurred in the
     * RunningComServer's
     * query api while executing queries.
     *
     * @return The number of event API failures
     */
     int getNumberOfFailures ();
}