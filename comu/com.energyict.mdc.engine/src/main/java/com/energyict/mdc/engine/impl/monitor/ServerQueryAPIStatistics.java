/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.monitor;

import com.energyict.mdc.engine.impl.tools.JmxStatistics;
import com.energyict.mdc.engine.monitor.QueryAPIStatistics;

public interface ServerQueryAPIStatistics extends QueryAPIStatistics{

    /**
     * Resets all counters that make sense to reset.
     * The number of clients will not be reset as that
     * can only be affected by clients that connect/disconnect.
     */
     void reset ();

    /**
     * Increments the number of clients that are connected to the
     * RunningComServer's
     * query api to execute queries.
     */
     void clientRegistered ();

    /**
     * Decrements the number of clients that are connected to the
     * RunningComServer's
     * query api to execute queries.
     */
     void clientUnregistered ();

    /**
     * Increments the counter that keeps track of the number
     * remote queries that have been executed.
     *
     * @param duration The number of milliseconds it took to execute the call
     */
     void callCompleted (long duration);

    /**
     * Increments the counter that keeps track of the number
     * remote queries that have been executed
     * and in addition increments the counter that keeps
     * track of the number of remote queries that failed
     * while executing.
     *
     * @param duration The number of milliseconds that were spent in the call while it lasted
     */
     void callFailed (long duration);

    /**
     * Gets the number of queries that have been collected by the
     * RunningComServer.
     *
     * @return The number of events that have been collected
     */
      JmxStatistics getCallStatistics ();

}
