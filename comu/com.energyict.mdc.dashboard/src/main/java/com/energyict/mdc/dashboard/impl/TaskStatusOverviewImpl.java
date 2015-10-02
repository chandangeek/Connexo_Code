package com.energyict.mdc.dashboard.impl;

import com.energyict.mdc.dashboard.TaskStatusOverview;
import com.energyict.mdc.device.data.tasks.TaskStatus;

/**
 * Provides an implementation for the {@link TaskStatusOverview} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-30 (11:53)
 */
public class TaskStatusOverviewImpl extends DashboardCountersImpl<TaskStatus> implements TaskStatusOverview {

    public TaskStatusOverviewImpl() {
        super();
    }

}