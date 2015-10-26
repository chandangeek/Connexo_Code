package com.energyict.mdc.device.data;

import java.io.Serializable;
import java.util.List;

/**
 * Defines a ConfigChange item that can be put on the queue
 */
public class ItemizeConfigChangeQueueMessage implements QueueMessage, Serializable {

    public long destinationDeviceConfigurationId;
    public List<String> deviceMRIDs;
    public DevicesForConfigChangeSearch search;

    public ItemizeConfigChangeQueueMessage(long destinationDeviceConfigurationId, List<String> deviceMRIDs, DevicesForConfigChangeSearch search) {
        this.destinationDeviceConfigurationId = destinationDeviceConfigurationId;
        this.deviceMRIDs = deviceMRIDs;
        this.search = search;
    }
}
