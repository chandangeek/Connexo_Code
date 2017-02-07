/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.tasks;

import com.energyict.mdc.device.data.QueueMessage;

/**
 * Created by bvn on 3/30/15.
 */
public class ItemizeCommunicationsFilterQueueMessage implements QueueMessage {
    public ComTaskExecutionFilterSpecificationMessage comTaskExecutionFilterSpecificationMessage;
    public String action;

    public ItemizeCommunicationsFilterQueueMessage() {
    }

    public ItemizeCommunicationsFilterQueueMessage(ComTaskExecutionFilterSpecificationMessage comTaskExecutionFilterSpecificationMessage, String action) {
        this.comTaskExecutionFilterSpecificationMessage = comTaskExecutionFilterSpecificationMessage;
        this.action = action;
    }
}

