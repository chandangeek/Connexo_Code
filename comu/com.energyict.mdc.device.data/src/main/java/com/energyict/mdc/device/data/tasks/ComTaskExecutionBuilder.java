/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.tasks;

import com.energyict.mdc.tasks.ComTask;

import aQute.bnd.annotation.ProviderType;

/**
 * Builder that supports basic value setters for a {@link ComTaskExecution}.
 */
@ProviderType
public interface ComTaskExecutionBuilder {

    ComTaskExecutionBuilder useDefaultConnectionTask(boolean useDefaultConnectionTask);

    /**
     * Explicitly setting a ConnectionTask will result in NOT using the default connectionTask.
     * This may be the default connectionTask, but if the default flag changes, then this ComTaskExecution
     * will still be marked to use the ConnectionTask from this setter.<br/>
     * Setting an Empty value will result in using the default ConnectionTask
     * <p/>
     * <i>If you want to use the default ConnectionTask, just set {@link #useDefaultConnectionTask(boolean)} to true</i>
     *
     * @param connectionTask the ConnectionTask to set
     * @return the current updater
     */
    ComTaskExecutionBuilder connectionTask(ConnectionTask<?, ?> connectionTask);

    ComTaskExecutionBuilder priority(int executionPriority);

    ComTaskExecutionBuilder ignoreNextExecutionSpecForInbound(boolean ignoreNextExecutionSpecsForInbound);

    // For adhoc comtaskExecutions
    public ComTaskExecutionBuilder scheduleNow();

    // For adhoc comtaskExecutions
    public ComTaskExecutionBuilder runNow();

    /**
     * Temporarily disables scheduling of the related {@link ComTask},
     * and will happily be ignored in the following circumstances:
     * <ul>
     * <li>the ComTask has been executed against the Device in an ad hoc way (see isAdhoc())</li>
     * <li>the ComTask is only available for execution but has no actual tasks against the Device (see isAvailable())</li>
     * </ul>
     * Note that putting a CommunicationTask that is not scheduled or is already on hold
     * will not cause any errors and will in fact be ignored.
     */
    public void putOnHold ();

    /**
     * Resumes the execution of this CommunicationTask, which is the reverse
     * operation of putting it on hold.
     * Note that resuming a CommunicationTask that has not been put on hold
     * will not cause any errors and will in fact be ignored.
     */
    public void resume ();

    /**
     * Creates the actual ComTaskExecution with the objects set in the builder.
     *
     * @return the newly created ComTaskExecution
     */
    ComTaskExecution add();

}