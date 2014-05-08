package com.energyict.mdc.engine.impl.monitor;

import com.energyict.mdc.engine.monitor.CollectedDataStorageStatistics;
import com.energyict.mdc.engine.monitor.EventAPIStatistics;
import com.energyict.mdc.engine.monitor.OperationalStatistics;
import com.energyict.mdc.engine.monitor.QueryAPIStatistics;

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