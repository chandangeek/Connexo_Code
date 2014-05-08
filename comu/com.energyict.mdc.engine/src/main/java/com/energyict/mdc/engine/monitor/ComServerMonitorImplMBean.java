package com.energyict.mdc.engine.monitor;

import javax.management.openmbean.CompositeData;

/**
 * Exposes the information that is gathered by the process
 * that monitors a RunningComServer
 * to a Jmx client.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-03 (09:26)
 */
public interface ComServerMonitorImplMBean {

    public CompositeData getOperationalStatisticsCompositeData ();

    public CompositeData getEventApiStatisticsCompositeData ();

    public CompositeData getQueryApiStatisticsCompositeData ();

    public CompositeData getCollectedDataStorageStatisticsCompositeData ();

    public void resetEventApiStatistics ();

    public void resetQueryApiStatistics ();

}