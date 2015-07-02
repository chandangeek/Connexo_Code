package com.energyict.mdc.device.data.tasks;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;

/**
 * Updater that supports basic value setters for a {@link ComTaskExecution}.
 */
@ProviderType
public interface ComTaskExecutionUpdater<U extends ComTaskExecutionUpdater<U, C>, C extends ComTaskExecution> {

    U useDefaultConnectionTask(boolean useDefaultConnectionTask);

    U useDefaultConnectionTask(ConnectionTask<?, ?> defaultConnectionTask);

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
    U connectionTask(ConnectionTask<?, ?> connectionTask);

    U priority(int plannedPriority);

    U ignoreNextExecutionSpecForInbound(boolean ignoreNextExecutionSpecsForInbound);

    /**
     * Sets the given nextExecutionTimeStamp and execution priority.
     *
     * @param nextExecutionTimestamp the timeStamp to set
     * @param executionPriority the changed execution priority
     * @return the current updater
     */
    U forceNextExecutionTimeStampAndPriority(Instant nextExecutionTimestamp, int executionPriority);

    /**
     * Updates the actual ComTaskExecution with the objects set in this builder
     *
     * @return the updated created ComTaskExecution
     */
    C update();

}