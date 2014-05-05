package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import java.util.List;

/**
 * A ComJob is a wrapper for a {@link ComTaskExecution} that is ready to be executed.
 * They will be returned by the {@link com.energyict.mdc.device.data.DeviceDataService}
 * when looking for work that can be executed on a {@link com.energyict.mdc.engine.model.ComPort}.
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
     * Tests if this ComJob represents a group of {@link ComTaskExecution}s.
     *
     * @return A flag that indicates if this ComJob contains more than one ScheduledComTask
     */
    public boolean isGroup ();

    /**
     * Gets the {@link ScheduledConnectionTask} to which all this
     * ComJob's ComTaskExecution relate.
     *
     * @return The ConnectionTask
     */
    public ScheduledConnectionTask getConnectionTask ();

    /**
     * Gets the {@link ComTaskExecution}s that need to be executed as part of this ComJob.
     *
     * @return The OutboundComTaskExecutions
     */
    public List<ComTaskExecution> getComTaskExecutions ();

}