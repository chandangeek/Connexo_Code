/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.monitor;

import com.energyict.mdc.engine.monitor.ScheduledComPortOperationalStatistics;

/**
 * Exposes the information that is gathered by the process
 * that monitors a ScheduledComPort.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-03 (13:03)
 */
public interface ScheduledComPortMBean {

    ScheduledComPortOperationalStatistics getOperationalStatistics ();

}