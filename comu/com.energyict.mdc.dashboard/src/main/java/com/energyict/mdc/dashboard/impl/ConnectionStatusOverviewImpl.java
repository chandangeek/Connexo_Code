package com.energyict.mdc.dashboard.impl;

import com.energyict.mdc.dashboard.ConnectionStatusOverview;
import com.energyict.mdc.dashboard.Counter;
import com.energyict.mdc.device.data.tasks.TaskStatus;

/**
 * Provides an implementation for the {@link ConnectionStatusOverview} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-30 (11:53)
 */
public class ConnectionStatusOverviewImpl extends DashboardCountersImpl<TaskStatus> implements ConnectionStatusOverview {

    public ConnectionStatusOverviewImpl() {
        super();
    }

    public ConnectionStatusOverviewImpl(Counter<TaskStatus>... counters) {
        super(counters);
    }

}