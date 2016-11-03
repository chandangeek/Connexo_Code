package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.device.data.QueueMessage;
import com.energyict.mdc.scheduling.ScheduleAction;
import com.energyict.mdc.scheduling.ScheduleAddStrategy;

/**
 * Created by bvn on 3/27/15.
 */
public class ComScheduleOnDeviceQueueMessage implements QueueMessage {
    public long comScheduleId;
    public String mRID;
    public ScheduleAction action;
    public ScheduleAddStrategy strategy;

    public ComScheduleOnDeviceQueueMessage() {
    }


    public ComScheduleOnDeviceQueueMessage(long comScheduleId, String mRID, ScheduleAction action, ScheduleAddStrategy scheduleAddStrategy) {
        this.comScheduleId = comScheduleId;
        this.mRID = mRID;
        this.action = action;
        this.strategy = scheduleAddStrategy;
    }
}
