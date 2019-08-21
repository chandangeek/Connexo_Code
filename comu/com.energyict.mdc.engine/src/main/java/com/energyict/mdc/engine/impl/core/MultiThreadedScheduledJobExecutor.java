/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.common.tasks.OutboundConnectionTask;

import java.util.List;

public interface MultiThreadedScheduledJobExecutor extends Runnable {

    /**
     * Tests if this ScheduledJob is currently executing and connected the specified {@link OutboundConnectionTask}.
     *
     * @param connectionTask The OutboundConnectionTask
     * @return A flag that indicates if one of the ComTaskExecutions is scheduled to be executed by this ScheduledJob
     */
    boolean isConnectedTo(OutboundConnectionTask connectionTask);

    ConnectionTask getConnectionTask();

    boolean isExecutingHighPriorityJob();

    boolean isExecutingOneOf(List<ComTaskExecution> comTaskExecutions);

    int getNumberOfTasks();

    boolean isExecutingParallelRootScheduledJob();
}
