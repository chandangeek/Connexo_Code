package com.energyict.mdc.device.data.tasks;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;

/**
 * Builder that supports basic value setters for a {@link ComTaskExecution}.
 */
@ProviderType
public interface ComTaskExecutionBuilder<C extends ComTaskExecution> {

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
    ComTaskExecutionBuilder<C> connectionTask(ConnectionTask<?, ?> connectionTask);

    ComTaskExecutionBuilder<C> priority(int executionPriority);

    ComTaskExecutionBuilder ignoreNextExecutionSpecForInbound(boolean ignoreNextExecutionSpecsForInbound);

    // For adhoc comtaskExecutions
    public ComTaskExecutionBuilder<C> scheduleNow();

    public ComTaskExecutionBuilder<C> schedule(Instant instant);

    // For adhoc comtaskExecutions
    public ComTaskExecutionBuilder<C> runNow();

    /**
     * Creates the actual ComTaskExecution with the objects set in the builder.
     *
     * @return the newly created ComTaskExecution
     */
    C add();

}