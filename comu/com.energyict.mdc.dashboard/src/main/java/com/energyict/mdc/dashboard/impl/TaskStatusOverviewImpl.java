/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard.impl;

import com.energyict.mdc.dashboard.TaskStatusOverview;
import com.energyict.mdc.device.data.tasks.TaskStatus;

import java.util.Map;

/**
 * Provides an implementation for the {@link TaskStatusOverview} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-30 (11:53)
 */
class TaskStatusOverviewImpl extends DashboardCountersImpl<TaskStatus> implements TaskStatusOverview {

    static TaskStatusOverviewImpl empty() {
        return new TaskStatusOverviewImpl();
    }

    public static TaskStatusOverviewImpl from(Map<TaskStatus, Long> statusCounters) {
        TaskStatusOverviewImpl overview = new TaskStatusOverviewImpl();
        for (TaskStatus taskStatus : TaskStatus.values()) {
            overview.add(new CounterImpl<>(taskStatus, statusCounters.get(taskStatus)));
        }
        return overview;
    }

    private TaskStatusOverviewImpl() {
        super();
    }

}