package com.energyict.mdc.device.data.tasks;

/**
 * Builder that supports basic value setters for a {@link ComTaskExecution}.
 */
public interface ComTaskExecutionBuilder<B extends ComTaskExecutionBuilder<B, C>, C extends ComTaskExecution> {

    B useDefaultConnectionTask(boolean useDefaultConnectionTask);

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
    B connectionTask(ConnectionTask<?, ?> connectionTask);

    B priority(int executionPriority);

    B ignoreNextExecutionSpecForInbound(boolean ignoreNextExecutionSpecsForInbound);

    /**
     * Creates the actual ComTaskExecution with the objects set in the builder.
     *
     * @return the newly created ComTaskExecution
     */
    C add();

}