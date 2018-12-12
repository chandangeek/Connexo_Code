/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.monitor;

import javax.management.openmbean.CompositeData;

/**
 * Exposes the information that is gathered by the process
 * that monitors a ScheduledComPort to a Jmx client.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-17 (09:42)
 */
public interface ScheduledComPortMonitorImplMBean {

    public CompositeData getOperationalStatisticsCompositeData();

}