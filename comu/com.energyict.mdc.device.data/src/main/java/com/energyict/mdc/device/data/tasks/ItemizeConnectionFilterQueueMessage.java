package com.energyict.mdc.device.data.tasks;

import com.energyict.mdc.device.data.QueueMessage;

/**
 * Created by bvn on 3/30/15.
 */
public class ItemizeConnectionFilterQueueMessage implements QueueMessage {
    public ConnectionTaskFilterSpecificationMessage connectionTaskFilterSpecification;
    public String action;

    public ItemizeConnectionFilterQueueMessage() {
    }

    public ItemizeConnectionFilterQueueMessage(ConnectionTaskFilterSpecificationMessage connectionTaskFilterSpecification, String action) {
        this.connectionTaskFilterSpecification = connectionTaskFilterSpecification;
        this.action = action;
    }
}

