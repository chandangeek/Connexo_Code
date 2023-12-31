/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.tasks;

import com.elster.jupiter.time.TemporalExpression;
import com.energyict.mdc.common.protocol.ConnectionFunction;
import com.energyict.mdc.common.scheduling.ComSchedule;

import aQute.bnd.annotation.ConsumerType;

import java.time.Instant;

/**
 * Updater that supports basic value setters for a {@link ComTaskExecution}.
 */
@ConsumerType
public interface ComTaskExecutionUpdater {

    ComTaskExecutionUpdater useDefaultConnectionTask(boolean useDefaultConnectionTask);

    /**
     * Set the {@link ConnectionFunction} for the {@link ComTaskExecution}.<br/>
     * This may be the default connectionTask, but if the default flag changes, then this ComTaskExecution
     * will still be marked to use the ConnectionTask corresponding to the {@link ConnectionFunction} from this setter.<br/>
     * Setting an Empty value will result in using the default ConnectionTask
     * <p>
     * <i>If you want to use the default ConnectionTask, set {@link #useDefaultConnectionTask(boolean)} to true</i>
     *
     * @param connectionFunction the ConnectionFunction to set
     * @return the current updater
     */
    ComTaskExecutionUpdater setConnectionFunction(ConnectionFunction connectionFunction);

    /**
     * Internal call, should not be in API
     */
    ComTaskExecutionUpdater useDefaultConnectionTask(ConnectionTask<?, ?> defaultConnectionTask);

    /**
     * Internal call, should not be in API
     */
    ComTaskExecutionUpdater useConnectionTaskBasedOnConnectionFunction(ConnectionTask<?, ?> connectionTask);

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

    /**
     * Set the boolean isTracing field on the ComTaskExecution
     * If isTracing is true, bytes that have been read or written to the device
     * will log to the file defined in the logging.properties
     *
     * @param traced the flag to set
     * @return the current updater
     */
    ComTaskExecutionUpdater setTraced(boolean traced);

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