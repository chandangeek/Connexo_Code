package com.energyict.mdc.device.data.impl.configchange;

import com.energyict.mdc.device.data.QueueMessage;

/**
 * Copyrights EnergyICT
 * Date: 29.10.15
 * Time: 09:59
 */
public class SingleConfigChangeQueueMessage implements QueueMessage {
    public String deviceMrid;
    public long destinationDeviceConfigurationId;
    public long deviceConfigChangeInActionId;

    public SingleConfigChangeQueueMessage(String deviceMrid, long destinationDeviceConfigurationId, long deviceConfigChangeInActionId) {
        this.deviceMrid = deviceMrid;
        this.destinationDeviceConfigurationId = destinationDeviceConfigurationId;
        this.deviceConfigChangeInActionId = deviceConfigChangeInActionId;
    }
}
