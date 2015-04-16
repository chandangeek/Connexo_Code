package com.energyict.mdc.device.data.tasks;

import com.energyict.mdc.device.data.QueueMessage;

/**
 * Created by bvn on 3/27/15.
 */
public class RescheduleConnectionTaskQueueMessage implements QueueMessage {
    public Long connectionTaskId;
    public String action;

    public RescheduleConnectionTaskQueueMessage() {
    }

    public RescheduleConnectionTaskQueueMessage(Long connectionTaskId, String action) {
        this.connectionTaskId = connectionTaskId;
        this.action = action;
    }
}
