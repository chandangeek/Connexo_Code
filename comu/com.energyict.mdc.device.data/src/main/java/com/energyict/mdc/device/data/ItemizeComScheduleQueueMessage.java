package com.energyict.mdc.device.data;

import com.energyict.mdc.device.data.ComScheduleOnDevicesFilterSpecification;
import com.energyict.mdc.device.data.QueueMessage;
import com.energyict.mdc.scheduling.ScheduleAction;

import java.util.List;

/**
 * Message send from REST to message handler: handler will itemize all ComSchedule / Device mentioned in this message
 * This message will always contain a list of schedules and (either a device filter or list of mRIDs)
 */
public class ItemizeComScheduleQueueMessage implements QueueMessage {
    public ComScheduleOnDevicesFilterSpecification filter;
    public List<String> deviceMRIDs;
    public List<Long> scheduleIds;
    public ScheduleAction action;

    public ItemizeComScheduleQueueMessage() {
    }
}

