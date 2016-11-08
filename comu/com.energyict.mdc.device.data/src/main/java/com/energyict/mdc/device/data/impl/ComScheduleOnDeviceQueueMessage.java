package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.device.data.QueueMessage;
import com.energyict.mdc.scheduling.ScheduleAction;

/**
 * Created by bvn on 3/27/15.
 */
public class ComScheduleOnDeviceQueueMessage implements QueueMessage {
    public long comScheduleId;
    public long deviceId;
    public ScheduleAction action;

    public ComScheduleOnDeviceQueueMessage() {
    }

    public ComScheduleOnDeviceQueueMessage(long comScheduleId, long deviceId, ScheduleAction action) {
        this.comScheduleId = comScheduleId;
        this.deviceId = deviceId;
        this.action = action;
    }
}
