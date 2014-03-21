package com.energyict.mdc.task.impl;

import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.protocol.api.device.offline.DeviceOfflineFlags;
import com.energyict.mdc.task.ClockTask;
import com.energyict.mdc.task.ClockTaskType;
import javax.inject.Inject;

/**
 * Implementation for a {@link com.energyict.mdc.task.ClockTask}.
 *
 * @author gna
 * @since 24/04/12 - 8:35
 */
@ValidClockTask
class ClockTaskImpl extends ProtocolTaskImpl implements ClockTask {

    private static final DeviceOfflineFlags FLAGS = new DeviceOfflineFlags();

    enum Fields {
        CLOCK_TASK_TYPE("clockTaskType"),
        MINIMUM_CLOCK_DIFF("minimumClockDiff"),
        MAXIMUM_CLOCK_DIFF("maximumClockDiff"),
        MAXIMUM_CLOCK_SHIFT("maximumClockShift");
        private final String objectFieldName;

        Fields(String javaFieldName) {
            this.objectFieldName = javaFieldName;
        }

        String fieldName() {
            return objectFieldName;
        }
    }
    private ClockTaskType clockTaskType;
    private TimeDuration minimumClockDiff;
    private TimeDuration maximumClockDiff;
    private TimeDuration maximumClockShift;

    @Inject
    ClockTaskImpl(DataModel dataModel) {
        super(dataModel);
        setFlags(FLAGS);
    }

    /**
     * Return the ClockTaskType for this task
     *
     * @return the ClockTaskType
     */
    @Override
    public ClockTaskType getClockTaskType() {
        return this.clockTaskType;
    }

    /**
     * Get the minimum clock difference a Device must have before setting the Clock.
     *
     * @return the minimum clock difference
     */
    @Override
    public TimeDuration getMinimumClockDifference() {
        return this.minimumClockDiff;
    }

    /**
     * Get the maximum clock difference a Device may have before setting the Clock.
     *
     * @return the maximum clock difference
     */
    @Override
    public TimeDuration getMaximumClockDifference() {
        return this.maximumClockDiff;
    }

    /**
     * Get the maximum shift which may be done by a Clock synchronization.
     *
     * @return the maximum clock shift
     */
    @Override
    public TimeDuration getMaximumClockShift() {
        return this.maximumClockShift;
    }

    @Override
    public void setClockTaskType(ClockTaskType clockTaskType) {
        this.clockTaskType = clockTaskType;
    }

    @Override
    public void setMinimumClockDifference(TimeDuration minimumClockDiff) {
        this.minimumClockDiff = minimumClockDiff;
    }

    @Override
    public void setMaximumClockDifference(TimeDuration maximumClockDiff) {
        this.maximumClockDiff = maximumClockDiff;
    }

    @Override
    public void setMaximumClockShift(TimeDuration maximumClockShift) {
        this.maximumClockShift = maximumClockShift;
    }
}