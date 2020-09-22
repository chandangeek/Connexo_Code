/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices;

import com.energyict.mdc.device.data.QueueMessage;

public class SAPRegisteredNotificationOnDeviceQueueMessage implements QueueMessage {
    public long deviceId;
    public long endPointId;

    public SAPRegisteredNotificationOnDeviceQueueMessage() {
    }

    public SAPRegisteredNotificationOnDeviceQueueMessage(long deviceId, long endPointId) {
        this.deviceId = deviceId;
        this.endPointId = endPointId;
    }
}
