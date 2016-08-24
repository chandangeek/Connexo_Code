package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutionToken;

import java.time.Instant;
import java.util.List;

/**
 * Models a job that is scheduled to be executed,
 * i.e. the related task's status would be {@link com.energyict.mdc.device.data.tasks.TaskStatus#Pending}.
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
     * Tests if this ScheduledJob is still {@link com.energyict.mdc.device.data.tasks.TaskStatus#Pending}.
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
     * of the {@link com.energyict.mdc.device.data.tasks.ConnectionTask} that will be responsible
     * for establishing the connection to execute this ScheduledJob.
     *
     * @return A flag that indicates if the current system timestamp is within the ConnectionTask's ComWindow
     */
    boolean isWithinComWindow();

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
     * Notifies the ScheduledJob that its execution is considered complete.
     */
    void completed();

    /**
     * Performs rescheduling of all {@link com.energyict.mdc.tasks.ComTask}s
     * after the succesful execution of this Job.
     *
     * @param comServerDAO The ComServerDAO
     */
    void reschedule(ComServerDAO comServerDAO);

    /**
     * Notifies the ScheduledJob that its execution is outside
     * the {@link com.energyict.mdc.common.ComWindow}
     * of the related {@link com.energyict.mdc.device.data.tasks.ConnectionTask}.
     */
    void outsideComWindow();

    /**
     * Notifies the ScheduledJob that its execution is considered a failure.
     *
     * @param reason The ExecutionFailureReason
     */
    void failed(Throwable t, ExecutionFailureReason reason);

    /**
     * Performs rescheduling of all {@link com.energyict.mdc.tasks.ComTask}s
     * (including the ones that did not execute) after a failure
     * while executing this job.
     *
     * @param comServerDAO The ComServerDAO
     * @param t The failure
     * @param rescheduleReason the reason for rescheduling
     */
    void reschedule(ComServerDAO comServerDAO, Throwable t, RescheduleBehavior.RescheduleReason rescheduleReason);

    /**
     * Performs rescheduling of all {@link com.energyict.mdc.tasks.ComTask}s of this Job
     * to the next occurrence of the {@link com.energyict.mdc.common.ComWindow}
     * because the current system timestamp is not or no longer within that window.
     */
    void rescheduleToNextComWindow(ComServerDAO comServerDAO);

    void rescheduleToNextComWindow(ComServerDAO comServerDAO, Instant startingPoint);

    /**
     * Adds the token for this ScheduledJob.
     *
     * @param deviceCommandExecutionToken The token
     */
    void setToken(DeviceCommandExecutionToken deviceCommandExecutionToken);

    /**
     * Gets the List of {@link ComTaskExecution}s that will
     * be executed by this ScheduledJob.
     *
     * @return The List of ComTaskExecution
     */
    List<ComTaskExecution> getComTaskExecutions();

}