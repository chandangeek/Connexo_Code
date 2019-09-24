/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.tasks;

import static com.energyict.mdc.common.tasks.TaskServiceKeys.CLOCK_TASK_TYPE_FORCE_CLOCK;
import static com.energyict.mdc.common.tasks.TaskServiceKeys.CLOCK_TASK_TYPE_SET_CLOCK;
import static com.energyict.mdc.common.tasks.TaskServiceKeys.CLOCK_TASK_TYPE_SYNCHRONIZE_CLOCK;

/**
 * Defines the type of the{@link ClockTask}
 *
 * @author gna
 * @since 19/04/12 - 15:37
 */
public enum ClockTaskType {

    /**
     * set the clock if the timeDifference is between the min/max boundary
     */
    SETCLOCK(1) {
        @Override
        public String toString () {
            return CLOCK_TASK_TYPE_SET_CLOCK;
        }
    },
    /**
     * set the clock, no matter what the timeDifference is
     */
    FORCECLOCK(2) {
        @Override
        public String toString () {
            return CLOCK_TASK_TYPE_FORCE_CLOCK;
        }
    },
    /**
     * synchronize the clock based on a predefined max. shift
     */
    SYNCHRONIZECLOCK(3) {
        @Override
        public String toString () {
            return CLOCK_TASK_TYPE_SYNCHRONIZE_CLOCK;
        }
    };

    private final int type;

    private ClockTaskType(final int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    /**
     * Find the ClockType based upon the value in the Database
     *
     * @param type the type from the database
     * @return the corresponding ClockTaskType
     */
    public static ClockTaskType valueFromDb(final int type) {
        for (ClockTaskType clockTaskType : values()) {
            if (clockTaskType.getType() == type) {
                return clockTaskType;
            }
        }
        throw new IllegalStateException("unknown clock type: " + type);
    }

}
