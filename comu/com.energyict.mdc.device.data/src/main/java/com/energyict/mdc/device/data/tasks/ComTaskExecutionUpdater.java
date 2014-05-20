package com.energyict.mdc.device.data.tasks;

import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.scheduling.NextExecutionSpecs;
import com.energyict.mdc.scheduling.TemporalExpression;
import java.util.Date;

/**
 * Updater that supports basic value setters for a ComTaskExecution
 */
public interface ComTaskExecutionUpdater<U extends ComTaskExecutionUpdater<U, C>, C extends ComTaskExecution> {

    U setUseDefaultConnectionTaskFlag(boolean useDefaultConnectionTask);

    /**
     * Explicitly setting a ConnectionTask will result in NOT using the default connectionTask.
     * This may be the default connectionTask, but if the default flag changes, then this ComTaskExecution
     * will still be marked to use the ConnectionTask from this setter.<br/>
     * Setting an Empty value will result in using the default ConnectionTask
     * <p/>
     * <i>If you want to use the default ConnectionTask, just set {@link #setUseDefaultConnectionTaskFlag(boolean)} to true</i>
     *
     * @param connectionTask the ConnectionTask to set
     * @return the current updater
     */
    U setConnectionTask(ConnectionTask<?, ?> connectionTask);

    U setPriority(int executionPriority);

    U createOrUpdateNextExecutionSpec(TemporalExpression temporalExpression);

    U removeNextExecutionSpec();

    U setMasterNextExecutionSpec(NextExecutionSpecs masterNextExecutionSpec);

    U setIgnoreNextExecutionSpecForInbound(boolean ignoreNextExecutionSpecsForInbound);

    U setProtocolDialectConfigurationProperties(ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties);

    /**
     * Sets the given nextExecutionTimeStamp and priority
     *
     * @param nextExecutionTimestamp the timeStamp to set
     * @param priority the priority to set
     * @return the current updater
     */
    U setNextExecutionTimeStampAndPriority(Date nextExecutionTimestamp, int priority);

    U setUseDefaultConnectionTask(ConnectionTask<?, ?> defaultConnectionTask);

    /**
     * Updates the actual ComTaskExecution with the objects set in this builder
     *
     * @return the updated created ComTaskExecution
     */
    ComTaskExecution update();
}
