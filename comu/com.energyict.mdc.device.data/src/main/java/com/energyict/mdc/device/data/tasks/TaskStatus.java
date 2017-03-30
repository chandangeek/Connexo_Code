/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.tasks;

/**
 * Models the status of meter data collection tasks.
 * This <a href="TaskStatus.png">diagram</a> depicts
 * a state diagram that explains how the status
 * of meter data collection tasks will/can change
 * over time.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-05 (15:55)
 */
public enum TaskStatus {

    /**
     * Initial state, indicating that the task has never before
     * completed successfully. As long as the task is in this state,
     * getting the last successful completion timestamp will
     * return <code>null</code>. That will change as soon as
     * the task completed successfully for the first time.
     * From that point in time onwards, getting the last
     * successful completion timestamp will NOT return <code>null</code>
     * even when the task fails from time to time.
     * <p>
     * Note that this state is a virtual state for scheduled tasks,
     * i.e. for tasks that have a next execution time.
     * This state will automatically upgrade to either {@link #Waiting}
     * or {@link #Pending}, depending on whether the next execution timestamp
     * is in the future (Waiting) or past (Pending).
     */
    NeverCompleted,

    /**
     * Indicates that the task is waiting for execution to fire,
     * i.e. its next execution time is sometime in the future.
     */
    Waiting,

    /**
     * Indicates that the next execution timestamp of this task
     * is due and is therefore ready to execute but the task
     * has not been picked up by the execution engine yet.
     * In other words, the next execution timestamp is in the past
     * but the task is not execution yet.
     * When tasks remain in this state for a long time then
     * the execution engine's interval between checks for
     * tasks that are ready to execute is set too long.
     */
    Pending,

    /**
     * Indicates that the task is currently executing.
     */
    Busy,

    /**
     * Indicates that the task failed but the execution egine
     * will retry later because the number of execution attempts
     * has not been exceeded yet.
     * The amount of time that the execution engine will wait
     * before retrying can be configured system wide or
     * on the task itself.
     */
    Retrying,

    /**
     * Indicates that the task's execution failed even after
     * retrying the execution for the number of configured attempts.
     * Note that when the task has never completed successfully,
     * the state will be {@link #NeverCompleted} and not Failed in that case.
     */
    Failed,

    /**
     * Indicates that the user wanted to (temporarily) postpone the task's execution.
     * The execution will return back to normal by explicit trigger by the user.
     * When that happens, the state is likely to upgrade automatically
     * to {@link #Waiting} or {@link #Pending}, depending on whether the next execution
     * timestamp is in the future (Waiting) or past (Pending).
     * For {@link ConnectionTask}s reverting the situation back to normal is done
     * by resuming the task that simply clears the "pause" flag.
     * For {@link ComTaskExecution} this is done by rescheduling the task
     * which resets a next execution timestamp for the task.
     *
     * @see ConnectionTask#getStatus()
     * @see ConnectionTask#deactivate()
     * @see ConnectionTask#activate()
     * @see ComTaskExecution#isOnHold()
     * @see ComTaskExecution#scheduleNow()
     * @see ComTaskExecution#schedule(java.time.Instant)
     */
    OnHold,

    /**
     * Invalid state that should indicate that a particular communication task has some invalid 'state'
     */
    ProcessingError;

    public static TaskStatus initial () {
        return TaskStatus.NeverCompleted;
    }

}