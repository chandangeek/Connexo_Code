package com.energyict.mdc.device.data;

import com.elster.jupiter.metering.zone.ZoneAction;

public class ZoneOnDeviceQueueMessage  implements QueueMessage {
    public long zoneId;
    public long deviceId;
    public long zoneTypeId;
    public ZoneAction action;

    public ZoneOnDeviceQueueMessage() {
    }

    public ZoneOnDeviceQueueMessage(long deviceId, long zoneId, long zoneTypeId, ZoneAction action) {
        this.deviceId = deviceId;
        this.zoneId = zoneId;
        this.zoneTypeId = zoneTypeId;
        this.action = action;
    }
}
