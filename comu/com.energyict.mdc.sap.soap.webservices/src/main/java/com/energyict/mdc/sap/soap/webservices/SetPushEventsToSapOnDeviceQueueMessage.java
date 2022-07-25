/*
 * Copyright (c) 2022 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices;

import com.energyict.mdc.device.data.QueueMessage;

public class SetPushEventsToSapOnDeviceQueueMessage implements QueueMessage {
    public long deviceId;
    public boolean pushEventsToSap;

    // do not remove, it's needed for bulk action
    public SetPushEventsToSapOnDeviceQueueMessage() {
    }

    public SetPushEventsToSapOnDeviceQueueMessage(long deviceId, boolean pushEventsToSap) {
        this.deviceId = deviceId;
        this.pushEventsToSap = pushEventsToSap;
    }
}