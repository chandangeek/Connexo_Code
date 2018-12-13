/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;

import java.util.List;

/**
 * A ComJob is a wrapper for a {@link ComTaskExecution} that is ready to be executed.
 * They will be returned by the {@link DeviceService}
 * when looking for work that can be executed on a {@link com.energyict.mdc.engine.config.ComPort}.
 * When the ComPort supports simultaneous connections but the available
 * ScheduledComTasks are linked to an {@link ScheduledConnectionTask}
 * that does not support simultaneous connections, a ComJob is actually a group
 * of ComTaskExecution that need to be executed one after the other with a single connection.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-07-20 (13:59)
 */
public interface ComJob {

    /**
     * Gets the {@link ScheduledConnectionTask} to which all this
     * ComJob's ComTaskExecution relate.
     *
     * @return The ConnectionTask
     */
    long getConnectionTaskId();

    /**
     * Gets the {@link ComTaskExecution}s that need to be executed as part of this ComJob.
     *
     * @return The OutboundComTaskExecutions
     */
    List<ComTaskExecution> getComTaskExecutions();

}