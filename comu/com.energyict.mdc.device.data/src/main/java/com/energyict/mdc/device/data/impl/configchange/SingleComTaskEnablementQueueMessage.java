/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.configchange;

import com.energyict.mdc.device.data.QueueMessage;

/**
 * @author sva
 * @since 2016-01-25 - 11:51
 */
public class SingleComTaskEnablementQueueMessage implements QueueMessage {

    public long deviceId;
    public long comTaskEnablementId;

    @SuppressWarnings("unused")
    public SingleComTaskEnablementQueueMessage() {
    }

    public SingleComTaskEnablementQueueMessage(long deviceId, long comTaskEnablementId) {
        this.deviceId = deviceId;
        this.comTaskEnablementId = comTaskEnablementId;
    }
}