/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard;

import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.common.tasks.TaskStatus;

import aQute.bnd.annotation.ProviderType;

/**
 * Models the overview of the {@link ConnectionTask}s
 * or {@link ComTaskExecution}s
 * that are configured and scheduled in the system.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-29 (17:36)
 */
@ProviderType
public interface TaskStatusOverview extends DashboardCounters<TaskStatus> {
}