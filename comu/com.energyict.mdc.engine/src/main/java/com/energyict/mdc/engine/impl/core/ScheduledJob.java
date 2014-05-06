package com.energyict.mdc.engine.impl.core;

import com.energyict.comserver.commands.DeviceCommandExecutionToken;

/**
 * Models a job that is scheduled to be executed,
 * i.e. the related task's status would be {@link com.energyict.mdc.tasks.TaskStatus#Pending}.
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
    public boolean attemptLock ();

    /**
     * Unlocks this job, basically undoing the effect
     * of the attemptLock method providing that that was successful.
     */
    public void unlock ();

    /**
     * Tests if this ScheduledJob is still {@link com.energyict.mdc.tasks.TaskStatus#Pending}.
     * This is possible when another thread has picked up
     * the same task and has already completed it while it was
     * waiting in your queue to be picked up.
     * In the case the ScheduledJob is no longer pending,
     * you should ignore the task and move on to the next job on your queue.
     *
     * @return A flag that indicates if this ScheduledJob is still pending
     */
    public boolean isStillPending ();

    /**
     * Tests if the current system timestamp is within the {@link com.energyict.mdc.tasks.ComWindow}
     * of the {@link com.energyict.mdc.tasks.ConnectionTask} that will be responsible
     * for establishing the connection to execute this ScheduledJob.
     *
     * @return A flag that indicates if the current system timestamp is within the ConnectionTask's ComWindow
     */
    public boolean isWithinComWindow ();

    /**
     * Executes this ScheduledJob on the precondition that
     * the attempt to acquire the lock succeeded.
     *
     * @see #attemptLock()
     */
    public void execute ();

    /**
     * Releases the {@link DeviceCommandExecutionToken}
     * that represents execution resources that are allocated
     * by a {@link com.energyict.comserver.commands.DeviceCommandExecutor}
     * to execute this ScheduledJob.
     */
    public void releaseToken ();

    /**
     * Returns the {@link DeviceCommandExecutionToken}
     * that represents execution resources that are allocated
     * by a {@link com.energyict.comserver.commands.DeviceCommandExecutor}
     * to execute this ScheduledJob.
     *
     * @return The DeviceCommandExecutionToken
     */
    public DeviceCommandExecutionToken getToken ();

    /**
     * Performs rescheduling of all {@link com.energyict.mdc.tasks.ComTask}s
     * after the succesful execution of this Job.
     */
    public void reschedule ();

    /**
     * Performs rescheduling of all {@link com.energyict.mdc.tasks.ComTask}s
     * (including the ones that did not executed...) after a failure
     * while executing this job.
     *
     * @param t The failure
     * @param rescheduleReason the reason for rescheduling
     */
    public void reschedule (Throwable t, RescheduleBehavior.RescheduleReason rescheduleReason);

    /**
     * Performs rescheduling of all {@link com.energyict.mdc.tasks.ComTask}s of this Job
     * to the next occurrence of the {@link com.energyict.mdc.tasks.ComWindow}
     * because the current system timestamp is not or no longer within that window.
     */
    public void rescheduleToNextComWindow ();

    /**
     * Adds the token for this ScheduledJob
     * @param deviceCommandExecutionToken
     */
    public void setToken(DeviceCommandExecutionToken deviceCommandExecutionToken);
}