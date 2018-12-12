/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.monitor;

import com.energyict.mdc.engine.monitor.OperationalStatistics;

import java.util.Date;

public interface ServerOperationalStatistics extends OperationalStatistics {

    /**
     * Sets the timestamp on which the RunningComServer last checked for changes.
     *
     * @param lastCheckForChangesTimestamp The timestamp on which the RunningComServer last checked for changes
     */
     void setLastCheckForChangesTimestamp (Date lastCheckForChangesTimestamp);

}
