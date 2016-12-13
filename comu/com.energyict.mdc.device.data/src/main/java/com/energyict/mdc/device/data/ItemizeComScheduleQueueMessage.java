package com.energyict.mdc.device.data;

import com.energyict.mdc.scheduling.ScheduleAction;
import com.energyict.mdc.scheduling.ScheduleAddStrategy;

import java.util.List;

/**
 * Message send from REST to message handler: handler will itemize all ComSchedule / Device mentioned in this message
 * This message will always contain a list of schedules and (either a device filter or list of mRIDs)
 */
public class ItemizeComScheduleQueueMessage implements QueueMessage {
    public ComScheduleOnDevicesFilterSpecification filter;
    public List<Long> deviceIds;
    public List<Long> scheduleIds;
    public ScheduleAction action;
    public ScheduleAddStrategy strategy;

    public ItemizeComScheduleQueueMessage() {
    }
}

