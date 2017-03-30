/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dashboard.impl;

import com.energyict.mdc.dashboard.DeviceTypeBreakdown;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.tasks.TaskStatus;

import java.util.Map;

/**
 * Provides an implementation for the {@link DeviceTypeBreakdown} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-30 (14:03)
 */
class DeviceTypeBreakdownImpl extends TaskStatusBreakdownCountersImpl<DeviceType> implements DeviceTypeBreakdown {

    public static DeviceTypeBreakdownImpl from(Map<DeviceType, Map<TaskStatus, Long>> rawData) {
        DeviceTypeBreakdownImpl breakdown = new DeviceTypeBreakdownImpl();
        for (DeviceType deviceType : rawData.keySet()) {
            Map<TaskStatus, Long> statusCount = rawData.get(deviceType);
            breakdown.add(new TaskStatusBreakdownCounterImpl<>(
                    deviceType,
                    TaskStatusses.SUCCESS.count(statusCount),
                    TaskStatusses.FAILED.count(statusCount),
                    TaskStatusses.PENDING.count(statusCount)));
        }
        return breakdown;
    }

    static DeviceTypeBreakdownImpl empty() {
        return new DeviceTypeBreakdownImpl();
    }

    private DeviceTypeBreakdownImpl() {
        super();
    }

}