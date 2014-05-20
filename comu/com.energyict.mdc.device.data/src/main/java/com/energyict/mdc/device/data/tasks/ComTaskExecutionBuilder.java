package com.energyict.mdc.device.data.tasks;

import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.data.impl.tasks.ComTaskExecutionImpl;
import com.energyict.mdc.scheduling.NextExecutionSpecs;
import com.energyict.mdc.scheduling.TemporalExpression;

/**
     * Builder that supports basic value setters for a ComTaskExecution
     */
public interface ComTaskExecutionBuilder<B extends ComTaskExecutionBuilder<B, C>, C extends ComTaskExecutionImpl> {

    B setUseDefaultConnectionTask(boolean useDefaultConnectionTask);

    /**
     * Explicitly setting a ConnectionTask will result in NOT using the default connectionTask.
     * This may be the default connectionTask, but if the default flag changes, then this ComTaskExecution
     * will still be marked to use the ConnectionTask from this setter.<br/>
     * Setting an Empty value will result in using the default ConnectionTask
     * <p/>
     * <i>If you want to use the default ConnectionTask, just set {@link #setUseDefaultConnectionTask(boolean)} to true</i>
     *
     * @param connectionTask the ConnectionTask to set
     * @return the current updater
     */
    B setConnectionTask(ConnectionTask<?, ?> connectionTask);

    B setPriority(int executionPriority);

    B createNextExecutionSpec(TemporalExpression temporalExpression);

    B setMasterNextExecutionSpec(NextExecutionSpecs masterNextExecutionSpec);

    B setIgnoreNextExecutionSpecForInbound(boolean ignoreNextExecutionSpecsForInbound);

    B setProtocolDialectConfigurationProperties(ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties);

    /**
     * Creates the actual ComTaskExecution with the objects set in the builder
     *
     * @return the newly created ComTaskExecution
     */
    C add();
}
