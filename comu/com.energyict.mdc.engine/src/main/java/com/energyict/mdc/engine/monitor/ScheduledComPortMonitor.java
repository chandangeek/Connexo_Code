/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.monitor;

import com.energyict.mdc.engine.config.ComPort;

import aQute.bnd.annotation.ProviderType;

/**
 * Exposes the information that is gathered by the process
 * that monitors a ScheduledComPort.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-17 (09:43)
 */
@ProviderType
public interface ScheduledComPortMonitor {

    /**
     * Verifies if this monitors the given comport
     * @param comport to test
     * @return true is this monitor monitors the given port
     */
    boolean isMonitoring(ComPort comport);

    /**
     * The {@link ScheduledComPortOperationalStatistics} as a result of the monitoring of the Scheduled Comport
     * @return the operational statistics
     */
    ScheduledComPortOperationalStatistics getOperationalStatistics();

}