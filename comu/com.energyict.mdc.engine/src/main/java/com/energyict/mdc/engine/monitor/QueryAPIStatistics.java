package com.energyict.mdc.engine.monitor;


import com.energyict.mdc.engine.impl.tools.JmxStatistics;

import java.util.Date;
import java.util.Map;

/**
 * Models the statistics of the query API that are gathered by a ComServer process
 * (com.energyict.comserver.scheduling.RunningComServer).
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-03 (11:09)
 */
public interface QueryAPIStatistics {

    /**
     * Resets all counters that make sense to reset.
     * The number of clients will not be reset as that
     * can only be affected by clients that connect/disconnect.
     */
    public void reset();

    /**
     * Get A Map of clents (remote comserver) connected to the Running ComServer
     * together with the date the client last communicated.
     */
    public Map<String, Date> getRegisteredClients();

    /**
     * Gets the number of clients that are connected to the ComServer process's
      * (com.energyict.comserver.scheduling.RunningComServer).
     * query api to execute queries.
     *
     * @return The number of event API clients
     */
    public int getNumberOfClients();

    /**
     * Gets the number of failures that have occurred in the ComServer process's
           * (com.energyict.comserver.scheduling.RunningComServer).
     * query api while executing queries.
     *
     * @return The number of event API failures
     */
    public int getNumberOfFailures();

    /**
     * Increments the number of clients that are connected to the ComServer process's
     * (com.energyict.comserver.scheduling.RunningComServer).
     * query api to execute queries.
     *
     * @param clientName name of the client (remote comserver) to register
     * @param lastSeen Date the connection is made;
     */
    public void clientRegistered(String clientName, Date lastSeen);

    /**
     * Decrements the number of clients that are connected to the ComServer process's
     * (com.energyict.comserver.scheduling.RunningComServer).
     * query api to execute queries.
     *
     * @param clientName name of the client (remote comserver) to unregister
     */
    public void clientUnregistered(String clientName);

    /**
     * Increments the counter that keeps track of the number
     * remote queries that have been executed.
     *
     * @param duration The number of milliseconds it took to execute the call
     */
    public void callCompleted(long duration);

    /**
     * Increments the counter that keeps track of the number
     * remote queries that have been executed
     * and in addition increments the counter that keeps
     * track of the number of remote queries that failed
     * while executing.
     *
     * @param duration The number of milliseconds that were spent in the call while it lasted
     */
    public void callFailed(long duration);

    /**
     * Gets the number of queries that have been collected by the ComServer process's
     * (com.energyict.comserver.scheduling.RunningComServer).
     *
     * @return The number of events that have been collected
     */
    public JmxStatistics getCallStatistics();


}