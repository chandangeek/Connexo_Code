/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data;

import com.energyict.mdc.device.data.impl.configchange.ServerDeviceForConfigChange;

import java.util.List;

/**
 * Defines a ConfigChange item that can be put on the queue
 */
public class ItemizeConfigChangeQueueMessage implements QueueMessage {

    public String topic = ServerDeviceForConfigChange.DEVICE_CONFIG_CHANGE_BULK_SETUP_ACTION;
    public long destinationDeviceConfigurationId;
    public long deviceConfigChangeRequestId;
    public List<Long> deviceIds;
    public DevicesForConfigChangeSearch search;

    @SuppressWarnings("unused")
    public ItemizeConfigChangeQueueMessage() {
    }

    public ItemizeConfigChangeQueueMessage(long destinationDeviceConfigurationId, List<Long> deviceIds, DevicesForConfigChangeSearch search, long deviceConfigChangeRequestId) {
        this.destinationDeviceConfigurationId = destinationDeviceConfigurationId;
        this.deviceIds = deviceIds;
        this.search = search;
        this.deviceConfigChangeRequestId = deviceConfigChangeRequestId;
    }
}
