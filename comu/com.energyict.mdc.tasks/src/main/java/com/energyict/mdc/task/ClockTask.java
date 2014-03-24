package com.energyict.mdc.task;

import com.energyict.mdc.common.TimeDuration;

/**
 * Models the {@link com.energyict.mdc.task.ProtocolTask} which can manipulate the Clock of a Device.
 *
 * @author gna
 * @since 19/04/12 - 15:23
 */
public interface ClockTask extends ProtocolTask {

    /**
     * Return the ClockTaskType for this task
     *
     * @return the ClockTaskType
     */
    public ClockTaskType getClockTaskType();
    public void setClockTaskType(ClockTaskType clockTaskType);

    /**
     * Get the minimum clock difference a Device must have before setting/synchronizing the Clock.
     *
     * @return the minimum clock difference
     */
    public TimeDuration getMinimumClockDifference();
    public void setMinimumClockDifference(TimeDuration minimumClockDiff);

    /**
     * Get the maximum clock difference a Device may have before setting the Clock.
     *
     * @return the maximum clock difference
     */
    public TimeDuration getMaximumClockDifference();
    public void setMaximumClockDifference(TimeDuration maximumClockDiff);

    /**
     * Get the maximum shift which may be done by a Clock synchronization.
     *
     * @return the maximum clock shift
     */
    public TimeDuration getMaximumClockShift();
    public void setMaximumClockShift(TimeDuration maximumClockShift);

    interface ClockTaskBuilder {
        public ClockTaskBuilder minimumClockDifference(TimeDuration minimumClockDiff);
        public ClockTaskBuilder maximumClockDifference(TimeDuration maximumClockDiff);
        public ClockTaskBuilder maximumClockShift(TimeDuration maximumClockShift);
        public ClockTask add();
    }
}