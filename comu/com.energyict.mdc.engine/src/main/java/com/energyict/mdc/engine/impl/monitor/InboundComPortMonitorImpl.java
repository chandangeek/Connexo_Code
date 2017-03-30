/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.monitor;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.impl.core.ComPortListener;
import com.energyict.mdc.engine.monitor.InboundComPortMonitor;

import javax.management.openmbean.CompositeData;
import java.time.Clock;

/**
 * Provides an implementation for the {@link ScheduledComPortMonitorImplMBean} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-17 (10:11)
 */
public class InboundComPortMonitorImpl implements InboundComPortMonitorImplMBean, InboundComPortMonitor {

    private ComPortListener comPort;
    private InboundComPortOperationalStatisticsImpl operationalStatistics;

    public InboundComPortMonitorImpl(ComPortListener comPort, Clock clock, Thesaurus thesaurus) {
        super();
        this.comPort = comPort;
        this.operationalStatistics = new InboundComPortOperationalStatisticsImpl(clock, thesaurus);
    }

    public ComPortListener getComPort () {
        return this.comPort;
    }

    @Override
    public boolean isMonitoring(ComPort comport) {
        return this.comPort.getComPort().getId() == comport.getId();
    }

    @Override
    public InboundComPortOperationalStatisticsImpl getOperationalStatistics () {
        return this.operationalStatistics;
    }

    @Override
    public CompositeData getOperationalStatisticsCompositeData () {
        return this.operationalStatistics.toCompositeData();
    }


}