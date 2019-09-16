/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.common.tasks.ComTask;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.common.tasks.OutboundConnectionTask;
import com.energyict.mdc.common.tasks.TaskStatus;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutionToken;

import java.util.List;

/**
 * Models a job that is scheduled to be executed,
 * i.e. the related task's status would be {@link TaskStatus#Pending}.
 */
public interface ScheduledJob {

    /**
     * Attempts to lock the subject of this ScheduledJob
     * and returns <code>true</code> when the lock succeeds
     * and <code>false</code> when the lock fails.
     * Committing the transaction that obtains the lock
     * releases the lock.
     *
     * @return <code>true</code> iff the lock succeeds
     */
    boolean attemptLock();

    /**
     * Unlocks this job, basically undoing the effect
     * of the attemptLock method providing that that was successful.
     */
    void unlock();

    /**
     * Tests if this ScheduledJob is still {@link TaskStatus#Pending}.
     * This is possible when another thread has picked up
     * the same task and has already completed it while it was
     * waiting in your queue to be picked up.
     * In the case the ScheduledJob is no longer pending,
     * you should ignore the task and move on to the next job on your queue.
     *
     * @return A flag that indicates if this ScheduledJob is still pending
     */
    boolean isStillPending();

    /**
     * Tests if the current system timestamp is within the {@link com.energyict.mdc.common.ComWindow}
     * of the {@link ConnectionTask} that will be responsible
     * for establishing the connection to execute this ScheduledJob.
     *
     * @return A flag that indicates if the current system timestamp is within the ConnectionTask's ComWindow
     */
    boolean isWithinComWindow();

    /**
     * @return A flag that indicates if the {@link ScheduledJob} should be executed with high priority
     */
    boolean isHighPriorityJob();

    /**
     * Executes this ScheduledJob on the precondition that
     * the attempt to acquire the lock succeeded.
     *
     * @see #attemptLock()
     */
    void execute();

    /**
     * Releases the {@link DeviceCommandExecutionToken}
     * that represents execution resources that are allocated
     * by a {@link com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor}
     * to execute this ScheduledJob.
     */
    void releaseToken();

    /**
     * Returns the {@link DeviceCommandExecutionToken}
     * that represents execution resources that are allocated
     * by a {@link com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor}
     * to execute this ScheduledJob.
     *
     * @return The DeviceCommandExecutionToken
     */
    DeviceCommandExecutionToken getToken();

    /**
     * Adds the token for this ScheduledJob.
     *
     * @param deviceCommandExecutionToken The token
     */
    void setToken(DeviceCommandExecutionToken deviceCommandExecutionToken);

    /**
     * Performs rescheduling of all {@link ComTask}s
     * after the execution of this Job.
     */
    void reschedule();

    /**
     * Performs rescheduling of all {@link ComTask}s of this Job
     * to the next occurrence of the {@link com.energyict.mdc.common.ComWindow}
     * because the current system timestamp is not or no longer within that window.
     */
    void rescheduleToNextComWindow();

    /**
     * Tests if this ScheduledJob is scheduled to execute one of the specified {@link ComTaskExecution}s.
     *
     * @param comTaskExecutions The List of ComTaskExecution
     * @return A flag that indicates if one of the ComTaskExecutions is scheduled to be executed by this ScheduledJob
     */
    boolean containsOneOf(List<ComTaskExecution> comTaskExecutions);

    /**
     * Tests if this ScheduledJob is currently executing and connected the specified {@link OutboundConnectionTask}.
     *
     * @param connectionTask The OutboundConnectionTask
     * @return A flag that indicates if one of the ComTaskExecutions is scheduled to be executed by this ScheduledJob
     */
    boolean isConnectedTo(OutboundConnectionTask connectionTask);

    /**
     * Gets the {@link ConnectionTask} this {@link ScheduledJob} is currently connected to
     */
    ConnectionTask getConnectionTask();
}