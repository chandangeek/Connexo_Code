/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.configchange;

import com.energyict.mdc.device.data.QueueMessage;

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
