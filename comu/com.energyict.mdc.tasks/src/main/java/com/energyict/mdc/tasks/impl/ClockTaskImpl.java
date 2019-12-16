/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tasks.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.callback.PersistenceAware;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.tasks.ClockTask;
import com.energyict.mdc.common.tasks.ClockTaskType;
import com.energyict.mdc.common.tasks.TaskServiceKeys;
import com.energyict.mdc.upl.offline.DeviceOfflineFlags;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.Optional;

/**
 * Implementation for a {@link ClockTask}.
 *
 * @author gna
 * @since 24/04/12 - 8:35
 */
@ValidClockTask( groups = {Save.Create.class, Save.Update.class} )
class ClockTaskImpl extends ProtocolTaskImpl implements ClockTask, PersistenceAware {

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
    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{"+ TaskServiceKeys.CAN_NOT_BE_EMPTY +"}")
    private ClockTaskType clockTaskType;
    private TimeDuration minimumClockDiff;
    private TimeDuration maximumClockDiff;
    private TimeDuration maximumClockShift;

    public ClockTaskImpl() {
        super();
        setFlags(FLAGS);
    }

    @Inject
    ClockTaskImpl(DataModel dataModel) {
        super(dataModel);
        setFlags(FLAGS);
    }

    @Override
    public void postLoad() {
        this.minimumClockDiff = this.postLoad(this.minimumClockDiff);
        this.maximumClockDiff = this.postLoad(this.maximumClockDiff);
        this.maximumClockShift = this.postLoad(this.maximumClockShift);
    }

    @Override
    void deleteDependents() {
        // currently no dependents to delete
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
    public Optional<TimeDuration> getMinimumClockDifference() {
        return Optional.ofNullable(this.minimumClockDiff);
    }

    /**
     * Get the maximum clock difference a Device may have before setting the Clock.
     *
     * @return the maximum clock difference
     */
    @Override
    public Optional<TimeDuration> getMaximumClockDifference() {
        return Optional.ofNullable(this.maximumClockDiff);
    }

    /**
     * Get the maximum shift which may be done by a Clock synchronization.
     *
     * @return the maximum clock shift
     */
    @Override
    public Optional<TimeDuration> getMaximumClockShift() {
        return Optional.ofNullable(this.maximumClockShift);
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