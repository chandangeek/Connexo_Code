/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard.impl;

import com.energyict.mdc.common.comserver.ComPortPool;
import com.energyict.mdc.common.tasks.TaskStatus;
import com.energyict.mdc.dashboard.ComPortPoolBreakdown;

import java.util.Map;

/**
 * Provides an implementation for the {@link ComPortPoolBreakdown} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-30 (12:00)
 */
class ComPortPoolBreakdownImpl extends TaskStatusBreakdownCountersImpl<ComPortPool> implements ComPortPoolBreakdown {

    public static ComPortPoolBreakdownImpl from (Map<ComPortPool, Map<TaskStatus, Long>> rawData) {
        ComPortPoolBreakdownImpl breakdown = new ComPortPoolBreakdownImpl();
        for (ComPortPool comPortPool : rawData.keySet()) {
            Map<TaskStatus, Long> statusCount = rawData.get(comPortPool);
            breakdown.add(
                    new TaskStatusBreakdownCounterImpl<>(
                            comPortPool,
                            TaskStatusses.SUCCESS.count(statusCount),
                            TaskStatusses.FAILED.count(statusCount),
                            TaskStatusses.PENDING.count(statusCount)));
        }
        return breakdown;
    }

    private ComPortPoolBreakdownImpl() {
        super();
    }

}