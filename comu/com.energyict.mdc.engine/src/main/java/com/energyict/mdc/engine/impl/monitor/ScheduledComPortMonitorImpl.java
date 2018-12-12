/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.monitor;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.impl.core.ScheduledComPort;
import com.energyict.mdc.engine.monitor.ScheduledComPortMonitor;
import com.energyict.mdc.engine.monitor.ScheduledComPortOperationalStatistics;

import javax.management.openmbean.CompositeData;
import java.time.Clock;

/**
 * Provides an implementation for the {@link ScheduledComPortMonitorImplMBean} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-17 (10:11)
 */
public class ScheduledComPortMonitorImpl implements ScheduledComPortMonitorImplMBean, ScheduledComPortMonitor {

    private ScheduledComPort comPort;
    private ScheduledComPortOperationalStatisticsImpl operationalStatistics;

    public ScheduledComPortMonitorImpl(ScheduledComPort comPort, Clock clock, Thesaurus thesaurus) {
        super();
        this.comPort = comPort;
        this.operationalStatistics = new ScheduledComPortOperationalStatisticsImpl(comPort, clock, thesaurus);
    }

    public ScheduledComPort getComPort () {
        return this.comPort;
    }

    @Override
    public boolean isMonitoring(ComPort comport) {
        return this.comPort.getComPort().getId() == comport.getId();
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