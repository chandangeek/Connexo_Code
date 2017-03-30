/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard.impl;

import com.energyict.mdc.device.data.tasks.TaskStatus;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Provides a common definition for subsets of {@link TaskStatus}
 * and utility methods to count in terms of these subsets.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-09-22 (12:05)
 */
enum TaskStatusses {

    SUCCESS {
        @Override
        EnumSet<TaskStatus> taskStatusses() {
            return EnumSet.of(TaskStatus.Waiting);
        }
    },

    FAILED {
        @Override
        EnumSet<TaskStatus> taskStatusses() {
            return EnumSet.of(TaskStatus.Failed, TaskStatus.NeverCompleted);
        }
    },

    PENDING {
        @Override
        EnumSet<TaskStatus> taskStatusses() {
            return EnumSet.of(TaskStatus.Pending, TaskStatus.Busy, TaskStatus.Retrying);
        }
    };

    abstract EnumSet<TaskStatus> taskStatusses();

    long count(Map<TaskStatus, Long> statusCount) {
        return this.count(statusCount, this.taskStatusses());
    }

    private long count(Map<TaskStatus, Long> statusCount, Set<TaskStatus> taskStatusses) {
        long total = 0;
        for (TaskStatus taskStatus : taskStatusses) {
            total = total + statusCount.get(taskStatus);
        }
        return total;
    }

}