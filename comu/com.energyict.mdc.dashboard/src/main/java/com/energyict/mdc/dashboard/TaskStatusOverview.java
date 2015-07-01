package com.energyict.mdc.dashboard;

import com.energyict.mdc.device.data.tasks.TaskStatus;

import aQute.bnd.annotation.ProviderType;

/**
 * Models the overview of the {@link com.energyict.mdc.device.data.tasks.ConnectionTask}s
 * or {@link com.energyict.mdc.device.data.tasks.ComTaskExecution}s
 * that are configured and scheduled in the system.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-29 (17:36)
 */
@ProviderType
public interface TaskStatusOverview extends DashboardCounters<TaskStatus> {
}