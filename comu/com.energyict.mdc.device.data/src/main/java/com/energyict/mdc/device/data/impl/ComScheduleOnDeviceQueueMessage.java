package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.device.data.QueueMessage;
import com.energyict.mdc.scheduling.ScheduleAction;

/**
 * Created by bvn on 3/27/15.
 */
public class ComScheduleOnDeviceQueueMessage implements QueueMessage {
    public long comScheduleId;
    public String mRID;
    public ScheduleAction action;

    public ComScheduleOnDeviceQueueMessage() {
    }


    public ComScheduleOnDeviceQueueMessage(long comScheduleId, String mRID, ScheduleAction action) {
        this.comScheduleId = comScheduleId;
        this.mRID = mRID;
        this.action = action;
    }
}
