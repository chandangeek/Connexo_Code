/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.tasks.history;

import aQute.bnd.annotation.ProviderType;

/**
 * Models an overview of the different {@link com.energyict.mdc.device.data.tasks.ComTaskExecution tasks}
 * that have executed within a {@link ComSession}.
 * For now, it keeps track of
 * <ul>
 * <li>the number of tasks that completed successfully</li>
 * <li>the number of tasks whose execution failed</li>
 * <li>the number of tasks that were planned to execute but were not executed due to blocking failures or previous tasks</li>
 * </ul>
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-05-30 (08:35)
 */
@ProviderType
public interface TaskExecutionSummary {

    /**
     * Gets the number of {@link com.energyict.mdc.device.data.tasks.ComTaskExecution tasks}
     * that executed successfully within the {@link ComSession}.
     *
     * @return The number of successful tasks
     */
    int getNumberOfSuccessFulTasks();

    /**
     * Gets the number of {@link com.energyict.mdc.device.data.tasks.ComTaskExecution tasks}
     * whose execution failed within the {@link ComSession}.
     *
     * @return The number of failed tasks
     */
    int getNumberOfFailedTasks();

    /**
     * Gets the number of {@link com.energyict.mdc.device.data.tasks.ComTaskExecution tasks}
     * that were planned to be executed within the {@link ComSession}
     * but in the end were not executed due to blocking failures of other tasks.
     *
     * @return The number of successful tasks
     */
    int getNumberOfPlannedButNotExecutedTasks();

}