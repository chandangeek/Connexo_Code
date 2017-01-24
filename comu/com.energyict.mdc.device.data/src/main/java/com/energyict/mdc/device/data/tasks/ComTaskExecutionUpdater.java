package com.energyict.mdc.device.data.tasks;

import com.elster.jupiter.time.TemporalExpression;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.scheduling.model.ComSchedule;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;

/**
 * Updater that supports basic value setters for a {@link ComTaskExecution}.
 */
@ProviderType
public interface ComTaskExecutionUpdater {

    ComTaskExecutionUpdater useDefaultConnectionTask(boolean useDefaultConnectionTask);

    /**
     * Internal call, should not be in API
     */
    ComTaskExecutionUpdater useDefaultConnectionTask(ConnectionTask<?, ?> defaultConnectionTask);

    /**
     * Explicitly setting a ConnectionTask will result in NOT using the default connectionTask.
     * This may be the default connectionTask, but if the default flag changes, then this ComTaskExecution
     * will still be marked to use the ConnectionTask from this setter.<br/>
     * Setting an Empty value will result in using the default ConnectionTask
     * <p>
     * <i>If you want to use the default ConnectionTask, just set {@link #useDefaultConnectionTask(boolean)} to true</i>
     *
     * @param connectionTask the ConnectionTask to set
     * @return the current updater
     */
    ComTaskExecutionUpdater connectionTask(ConnectionTask<?, ?> connectionTask);

    ComTaskExecutionUpdater priority(int plannedPriority);

    ComTaskExecutionUpdater ignoreNextExecutionSpecForInbound(boolean ignoreNextExecutionSpecsForInbound);

    /**
     * Sets the given nextExecutionTimeStamp and execution priority.
     *
     * @param nextExecutionTimestamp the timeStamp to set
     * @param executionPriority the changed execution priority
     * @return the current updater
     */
    ComTaskExecutionUpdater forceNextExecutionTimeStampAndPriority(Instant nextExecutionTimestamp, int executionPriority);

    ComTaskExecutionUpdater forceLastExecutionStartTimestamp(Instant lastExecutionStartTimestamp);

    ComTaskExecutionUpdater calledByComTaskExecution();

    /**
     * Updates the actual ComTaskExecution with the objects set in this builder
     *
     * @return the updated created ComTaskExecution
     */
    ComTaskExecution update();

    /**
     * Updates the given fields in the actual ComTaskExecution with the values set in this builder
     *
     * @return the updated created ComTaskExecution
     */
    ComTaskExecution updateFields(String... fieldNames);

    ComTaskExecutionUpdater protocolDialectConfigurationProperties(ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties);

    /**
     * Sets the specifications for the calculation of the next
     * execution timestamp from the {@link TemporalExpression}.
     *
     * @param temporalExpression The TemporalExpression
     * @return The ManuallyScheduledComTaskExecutionUpdater
     */
    ComTaskExecutionUpdater createNextExecutionSpecs(TemporalExpression temporalExpression);

    /**
     * Removes the schedule and transforms the ComTaskExecution
     * into an adhoc scheduled ComTaskExecution.
     *
     * @return The ManuallyScheduledComTaskExecutionUpdater
     */
    ComTaskExecutionUpdater removeSchedule();

    /**
     * Remove the nextExecutionSpec from the ComTaskExecution
     *
     * @return the updater
     */
    ComTaskExecutionUpdater removeNextExecutionSpec();

    /**
     * Set the ComSchedule on the ComTaskExecution
     *
     * @param comSchedule the comSchedule
     * @return the updater
     */
    ComTaskExecutionUpdater addSchedule(ComSchedule comSchedule);

    ComTaskExecution getComTaskExecution();
}