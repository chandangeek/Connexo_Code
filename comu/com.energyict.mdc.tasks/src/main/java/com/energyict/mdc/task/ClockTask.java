package com.energyict.mdc.task;

import com.energyict.mdc.common.TimeDuration;

/**
 * Models the {@link com.energyict.mdc.task.ProtocolTask} which can manipulate the Clock of a Device.
 *
 * @author gna
 * @since 19/04/12 - 15:23
 */
public interface ClockTask extends ProtocolTask<ClockTask> {

    /**
     * Return the ClockTaskType for this task
     *
     * @return the ClockTaskType
     */
    public ClockTaskType getClockTaskType();

    /**
     * Get the minimum clock difference a Device must have before setting/synchronizing the Clock.
     *
     * @return the minimum clock difference
     */
    public TimeDuration getMinimumClockDifference();

    /**
     * Get the maximum clock difference a Device may have before setting the Clock.
     *
     * @return the maximum clock difference
     */
    public TimeDuration getMaximumClockDifference();

    /**
     * Get the maximum shift which may be done by a Clock synchronization.
     *
     * @return the maximum clock shift
     */
    public TimeDuration getMaximumClockShift();

    void setClockTaskType(ClockTaskType clockTaskType);

    void setMinimumClockDifference(TimeDuration minimumClockDiff);

    void setMaximumClockDifference(TimeDuration maximumClockDiff);

    void setMaximumClockShift(TimeDuration maximumClockShift);
}