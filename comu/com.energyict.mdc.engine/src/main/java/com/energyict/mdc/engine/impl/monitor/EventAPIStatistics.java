package com.energyict.mdc.engine.impl.monitor;

/**
 * Models the statistics of the event API that are gathered by the process
 * that monitors a RunningComServer.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-03 (11:09)
 */
public interface EventAPIStatistics {

    /**
     * Resets all counters that make sense to reset.
     * The number of clients will not be reset as that
     * can only be affected by clients that connect/disconnect.
     */
    public void reset ();

    /**
     * Gets the number of clients that are connected to the
     * RunningComServer's
     * event api to receive event notifications.
     *
     * @return The number of event API clients
     */
    public int getNumberOfClients ();

    /**
     * Increments the number of clients that have registered
     * in interest to receive events as they are being published.
     */
    public void clientRegistered ();

    /**
     * Decrements the number of clients that have registered
     * in interest to receive events as they are being published.
     */
    public void clientUnregistered ();

    /**
     * Gets the number of events that have been collected by the
     * RunningComServer.
     *
     * @return The number of events that have been collected
     */
    public long getNumberOfEvents ();

    /**
     * Increases the number of events that has been collected by the
     * RunningComServer.
     */
    public void eventWasPublished ();

}