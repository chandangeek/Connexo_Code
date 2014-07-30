package com.energyict.mdc.dashboard;

import com.energyict.mdc.device.data.tasks.TaskStatus;

/**
 * Counts things that relate to {@link TaskStatus}.
 * The counting is broken down to the different TaskStatusses.
 * For now, the counting is limited to:
 * <ul>
 * <li>{@link TaskStatus#Waiting Success}</li>
 * <li>{@link TaskStatus#Failed}</li>
 * <li>{@link TaskStatus#Pending}</li>
 * </ul>
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-30 (09:05)
 */
public interface TaskStatusBreakdownCounter<T> {

    /**
     * The number of times the target of this TaskStatusBreakdownCounter
     * relates to the successful execution of a task.
     *
     * @return The success count
     */
    public long getSuccessCount();

    /**
     * The number of times the target of this TaskStatusBreakdownCounter
     * relates to the failed execution of a task.
     *
     * @return The failure count
     * @see {@link TaskStatus#Failed}
     */
    public long getFailedCount ();

    /**
     * The number of times the target of this TaskStatusBreakdownCounter
     * relates to the execution of a task that is pending.
     *
     * @return The pending count
     * @see {@link TaskStatus#Pending}
     */
    public long getPendingCount ();

    /**
     * Returns the target of the counter, i.e. the object whose occurrence was counted.
     *
     * @return The target of the counter
     */
    public T getCountTarget();

}