/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.kpi;

import com.energyict.mdc.device.data.tasks.TaskStatus;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Wrapper for {@link TaskStatus}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-13 (15:56)
 */
public enum MonitoredTaskStatus {
    Total {
        @Override
        public Set<TaskStatus> monitoredStatusses() {
            // All but OnHold
            return EnumSet.complementOf(EnumSet.of(TaskStatus.OnHold));
        }
    },

    Success {
        @Override
        public Set<TaskStatus> monitoredStatusses() {
            return EnumSet.of(TaskStatus.Waiting);
        }
    },

    Ongoing {
        @Override
        public Set<TaskStatus> monitoredStatusses() {
            return EnumSet.of(TaskStatus.Pending, TaskStatus.Busy, TaskStatus.Retrying);
        }
    },

    Failed {
        @Override
        public Set<TaskStatus> monitoredStatusses() {
            return EnumSet.of(TaskStatus.Failed, TaskStatus.NeverCompleted);
        }
    };

    public abstract Set<TaskStatus> monitoredStatusses();

    public long calculateFrom(Map<TaskStatus, Long> statusCounters) {
        return this.monitoredStatusses().stream()
                    .mapToLong(statusCounters::get)
                    .sum();
    }

}