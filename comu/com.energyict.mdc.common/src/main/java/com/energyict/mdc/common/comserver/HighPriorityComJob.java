/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.comserver;

import com.energyict.mdc.common.device.data.ScheduledConnectionTask;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.common.tasks.PriorityComTaskExecutionLink;

import java.util.List;

/**
 * Created by Jozsef Szekrenyes on 06/02/2019.
 */
public interface HighPriorityComJob {

    /**
     * Tests if this HighPriorityComJob represents a group of {@link PriorityComTaskExecutionLink}s.
     *
     * @return A flag that indicates if this HighPriorityComJob contains more than one HighPriorityComTaskExecution
     */
    boolean isGroup();

    /**
     * Gets the {@link ConnectionTask} to which all this
     * HighPriorityComJob's HighPriorityComTaskExecution relate.
     *
     * @return The OutboundConnectionTask
     */
    ScheduledConnectionTask getConnectionTask();

    /**
     * Gets the {@link PriorityComTaskExecutionLink}s that need to be executed as part of this HighPriorityComJob.
     *
     * @return The List of HighPriorityComTaskExecution
     */
    List<PriorityComTaskExecutionLink> getPriorityComTaskExecutionLinks();

    /**
     * Gets the {@link ComTaskExecution}s of the {@link PriorityComTaskExecutionLink}s
     * that need to be executed as part of this HighPriorityComJob.
     *
     * @return The List of ComTaskExecution
     */
    List<ComTaskExecution> getComTaskExecutions();
}
