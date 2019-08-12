/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard.impl;

import com.energyict.mdc.common.protocol.ConnectionTypePluggableClass;
import com.energyict.mdc.common.tasks.TaskStatus;
import com.energyict.mdc.dashboard.ConnectionTypeBreakdown;

import java.util.Map;

/**
 * Provides an implementation for the {@link ConnectionTypeBreakdown} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-30 (13:50)
 */
class ConnectionTypeBreakdownImpl extends TaskStatusBreakdownCountersImpl<ConnectionTypePluggableClass> implements ConnectionTypeBreakdown {

    public static ConnectionTypeBreakdownImpl from(Map<ConnectionTypePluggableClass, Map<TaskStatus, Long>> rawData) {
        ConnectionTypeBreakdownImpl breakdown = new ConnectionTypeBreakdownImpl();
        for (ConnectionTypePluggableClass connectionTypePluggableClass : rawData.keySet()) {
            Map<TaskStatus, Long> statusCount = rawData.get(connectionTypePluggableClass);
            breakdown.add(
                    new TaskStatusBreakdownCounterImpl<>(
                            connectionTypePluggableClass,
                            TaskStatusses.SUCCESS.count(statusCount),
                            TaskStatusses.FAILED.count(statusCount),
                            TaskStatusses.PENDING.count(statusCount)));
        }
        return breakdown;
    }

    private ConnectionTypeBreakdownImpl() {
        super();
    }

}