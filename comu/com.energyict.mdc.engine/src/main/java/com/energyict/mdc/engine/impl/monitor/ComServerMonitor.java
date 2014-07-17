package com.energyict.mdc.engine.impl.monitor;

/**
 * Exposes the information that is gathered by the process
 * that monitors a RunningComServer.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-03 (09:26)
 */
public interface ComServerMonitor {

    public OperationalStatistics getOperationalStatistics ();

    public EventAPIStatistics getEventApiStatistics ();

    public QueryAPIStatistics getQueryApiStatistics ();

    public CollectedDataStorageStatistics getCollectedDataStorageStatistics ();

}