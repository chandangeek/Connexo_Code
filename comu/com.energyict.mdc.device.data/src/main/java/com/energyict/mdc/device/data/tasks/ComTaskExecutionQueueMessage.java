/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.tasks;

import com.energyict.mdc.device.data.QueueMessage;

/**
 * Created by bvn on 3/27/15.
 */
public class ComTaskExecutionQueueMessage implements QueueMessage {
    public Long comTaskExecId;
    public String action;

    public ComTaskExecutionQueueMessage() {
    }

    public ComTaskExecutionQueueMessage(Long comTaskExecId, String action) {
        this.comTaskExecId = comTaskExecId;
        this.action = action;
    }
}
