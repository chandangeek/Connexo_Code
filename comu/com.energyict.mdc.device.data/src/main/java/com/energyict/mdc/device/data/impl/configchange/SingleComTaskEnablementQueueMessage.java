package com.energyict.mdc.device.data.impl.configchange;

import com.energyict.mdc.device.data.QueueMessage;

/**
 * @author sva
 * @since 2016-01-25 - 11:51
 */
public class SingleComTaskEnablementQueueMessage implements QueueMessage {

    public String deviceMrid;
    public long comTaskEnablementId;

    @SuppressWarnings("unused")
    public SingleComTaskEnablementQueueMessage() {
    }

    public SingleComTaskEnablementQueueMessage(String deviceMrid, long comTaskEnablementId) {
        this.deviceMrid = deviceMrid;
        this.comTaskEnablementId = comTaskEnablementId;
    }
}