package com.energyict.mdc.engine.impl.monitor;

import com.energyict.mdc.engine.impl.core.RunningComServer;
import com.energyict.mdc.engine.monitor.CollectedDataStorageStatistics;
import com.energyict.mdc.engine.monitor.ComServerMonitorImplMBean;
import com.energyict.mdc.engine.monitor.ComServerOperationalStatistics;
import com.energyict.mdc.engine.monitor.EventAPIStatistics;
import com.energyict.mdc.engine.monitor.QueryAPIStatistics;

import javax.management.openmbean.CompositeData;

/**
 * Provides an implementation for the {@link ComServerMonitorImplMBean} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-03 (14:08)
 */
public class ComServerMonitorImpl implements ComServerMonitorImplMBean, ComServerMonitor {

    private RunningComServer comServer;
    private ComServerOperationalStatisticsImpl operationalStatistics;
    private EventAPIStatisticsImpl eventAPIStatistics;
    private QueryAPIStatisticsImpl queryAPIStatistics;
    private CollectedDataStorageStatisticsImpl collectedDataStorageStatistics;

    public ComServerMonitorImpl (RunningComServer comServer) {
        super();
        this.comServer = comServer;
        this.operationalStatistics = new ComServerOperationalStatisticsImpl(comServer);
        this.eventAPIStatistics = new EventAPIStatisticsImpl();
        if (comServer.isRemoteQueryApiStarted()) {
            this.queryAPIStatistics = new QueryAPIStatisticsImpl(comServer.getComServer());
        }
        this.collectedDataStorageStatistics = new CollectedDataStorageStatisticsImpl(comServer);
    }

    public RunningComServer getComServer () {
        return comServer;
    }

    @Override
    public ComServerOperationalStatistics getOperationalStatistics () {
        return this.operationalStatistics;
    }

    @Override
    public CompositeData getOperationalStatisticsCompositeData () {
        return this.operationalStatistics.toCompositeData();
    }

    @Override
    public EventAPIStatistics getEventApiStatistics () {
        return this.eventAPIStatistics;
    }

    @Override
    public CompositeData getEventApiStatisticsCompositeData () {
        return this.eventAPIStatistics.toCompositeData();
    }

    @Override
    public QueryAPIStatistics getQueryApiStatistics () {
        return this.queryAPIStatistics;
    }

    @Override
    public CompositeData getQueryApiStatisticsCompositeData () {
        if (this.queryAPIStatistics != null) {
            return this.queryAPIStatistics.toCompositeData();
        }
        else {
            return null;
        }
    }

    @Override
    public CollectedDataStorageStatistics getCollectedDataStorageStatistics () {
        return this.collectedDataStorageStatistics;
    }

    @Override
    public CompositeData getCollectedDataStorageStatisticsCompositeData () {
        return this.collectedDataStorageStatistics.toCompositeData();
    }

    @Override
    public void resetEventApiStatistics () {
        this.eventAPIStatistics.reset();
    }

    @Override
    public void resetQueryApiStatistics () {
        if (this.comServer.isRemoteQueryApiStarted()) {
            this.queryAPIStatistics = new QueryAPIStatisticsImpl(this.comServer.getComServer());
        }
        else {
            this.queryAPIStatistics = null;
        }
    }

}