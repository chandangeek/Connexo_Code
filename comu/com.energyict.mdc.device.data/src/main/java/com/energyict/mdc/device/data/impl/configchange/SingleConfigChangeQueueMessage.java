package com.energyict.mdc.device.data.impl.configchange;

import com.energyict.mdc.device.data.QueueMessage;

/**
 * Copyrights EnergyICT
 * Date: 29.10.15
 * Time: 09:59
 */
public class SingleConfigChangeQueueMessage implements QueueMessage {
    public long deviceId;
    public long destinationDeviceConfigurationId;
    public long deviceConfigChangeInActionId;
    public long deviceConfigChangeRequestId;

    public SingleConfigChangeQueueMessage() {
    }

    public SingleConfigChangeQueueMessage(long deviceId, long destinationDeviceConfigurationId, long deviceConfigChangeInActionId, long deviceConfigChangeRequestId) {
        this.deviceId = deviceId;
        this.destinationDeviceConfigurationId = destinationDeviceConfigurationId;
        this.deviceConfigChangeInActionId = deviceConfigChangeInActionId;
        this.deviceConfigChangeRequestId = deviceConfigChangeRequestId;
    }
}
