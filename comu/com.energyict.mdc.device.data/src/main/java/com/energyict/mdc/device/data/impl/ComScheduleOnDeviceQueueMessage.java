/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.device.data.QueueMessage;
import com.energyict.mdc.scheduling.ScheduleAction;
import com.energyict.mdc.scheduling.ScheduleAddStrategy;

/**
 * Created by bvn on 3/27/15.
 */
public class ComScheduleOnDeviceQueueMessage implements QueueMessage {
    public long comScheduleId;
    public long deviceId;
    public ScheduleAction action;
    public ScheduleAddStrategy strategy;

    public ComScheduleOnDeviceQueueMessage() {
    }

    public ComScheduleOnDeviceQueueMessage(long comScheduleId, long deviceId, ScheduleAction action, ScheduleAddStrategy scheduleAddStrategy) {
        this.comScheduleId = comScheduleId;
        this.deviceId = deviceId;
        this.action = action;
        this.strategy = scheduleAddStrategy;
    }
}
