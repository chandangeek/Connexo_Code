package com.energyict.mdc.dashboard;

import com.energyict.mdc.device.data.tasks.TaskStatus;

/**
 * Models the overview of the {@link com.energyict.mdc.device.data.tasks.ConnectionTask}s
 * that are configured and scheduled in the system.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-29 (17:36)
 */
public interface ConnectionStatusOverview extends DashboardCounters<TaskStatus> {
}