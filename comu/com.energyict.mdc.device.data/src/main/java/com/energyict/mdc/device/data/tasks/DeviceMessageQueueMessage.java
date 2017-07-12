/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.tasks;

import com.energyict.mdc.device.data.QueueMessage;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by bvn on 3/27/15.
 */
public class DeviceMessageQueueMessage implements QueueMessage {
    public Long deviceId;
    public DeviceMessageId deviceMessageId;
    public long releaseDate;
    public final Map<String, String> properties = new HashMap<>();

    public DeviceMessageQueueMessage() {
    }

    public DeviceMessageQueueMessage(Long deviceId, DeviceMessageId deviceMessageId, long releaseDate, Map<String, String> properties) {
        this.deviceId = deviceId;
        this.deviceMessageId = deviceMessageId;
        this.releaseDate = releaseDate;
        this.properties.putAll(properties);
    }
}
