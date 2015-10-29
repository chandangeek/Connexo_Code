package com.energyict.mdc.device.data;

import java.util.List;

/**
 * Defines a ConfigChange item that can be put on the queue
 */
public class ItemizeConfigChangeQueueMessage implements QueueMessage {

    public long destinationDeviceConfigurationId;
    public long deviceConfigChangeRequestId;
    public List<String> deviceMRIDs;
    public DevicesForConfigChangeSearch search;

    public ItemizeConfigChangeQueueMessage(long destinationDeviceConfigurationId, List<String> deviceMRIDs, DevicesForConfigChangeSearch search, long deviceConfigChangeRequestId) {
        this.destinationDeviceConfigurationId = destinationDeviceConfigurationId;
        this.deviceMRIDs = deviceMRIDs;
        this.search = search;
        this.deviceConfigChangeRequestId = deviceConfigChangeRequestId;
    }
}
