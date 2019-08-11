/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.tasks;

import com.energyict.mdc.common.protocol.DeviceMessageId;
import com.energyict.mdc.device.data.QueueMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by bvn on 3/27/15.
 */
public class DeviceMessageQueueMessage implements QueueMessage {
    public Long deviceId;
    public DeviceMessageId deviceMessageId;
    public long releaseDate;
    public String createdByUser;
    public final Map<String, Object> properties = new HashMap<>();
    public boolean trigger;

    public DeviceMessageQueueMessage() {
    }

    public DeviceMessageQueueMessage(Long deviceId, DeviceMessageId deviceMessageId, long releaseDate, Map<String, Object> properties, String user, boolean trigger) {
        this.deviceId = deviceId;
        this.deviceMessageId = deviceMessageId;
        this.releaseDate = releaseDate;
        this.createdByUser = user;
        this.properties.putAll(properties);
        this.trigger = trigger;
    }
}
