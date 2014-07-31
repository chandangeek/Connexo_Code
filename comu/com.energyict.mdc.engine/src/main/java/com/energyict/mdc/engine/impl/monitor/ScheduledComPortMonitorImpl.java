package com.energyict.mdc.engine.impl.monitor;

import com.energyict.mdc.engine.impl.core.ScheduledComPort;

import com.elster.jupiter.util.time.Clock;

import javax.management.openmbean.CompositeData;

/**
 * Provides an implementation for the {@link ScheduledComPortMonitorImplMBean} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-17 (10:11)
 */
public class ScheduledComPortMonitorImpl implements ScheduledComPortMonitorImplMBean, ScheduledComPortMonitor {

    private final Clock clock;
    private ScheduledComPort comPort;
    private ScheduledComPortOperationalStatisticsImpl operationalStatistics;

    public ScheduledComPortMonitorImpl(ScheduledComPort comPort, Clock clock) {
        super();
        this.comPort = comPort;
        this.clock = clock;
        this.operationalStatistics = new ScheduledComPortOperationalStatisticsImpl(comPort, this.clock);
    }

    public ScheduledComPort getComPort () {
        return this.comPort;
    }

    @Override
    public ScheduledComPortOperationalStatistics getOperationalStatistics () {
        return this.operationalStatistics;
    }

    @Override
    public CompositeData getOperationalStatisticsCompositeData () {
        return this.operationalStatistics.toCompositeData();
    }

}