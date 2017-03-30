/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.monitor;

import com.energyict.mdc.engine.monitor.ScheduledComPortOperationalStatistics;

import java.util.Date;

public interface ServerScheduledComPortOperationalStatistics extends ScheduledComPortOperationalStatistics {

    /**
     * Sets the timestamp on which the ScheduledComPort last checked for work.
     *
     * @param lastCheckForWorkTimestamp The timestamp
     */
     void setLastCheckForWorkTimestamp(Date lastCheckForWorkTimestamp);
}
