package com.energyict.mdc.device.data;

public class LoadProfileOnDeviceQueueMessage implements QueueMessage {
    public long deviceId;
    public String loadProfileName;
    public long lastReading;

    public LoadProfileOnDeviceQueueMessage() {
    }

    public LoadProfileOnDeviceQueueMessage(long deviceId, String loadProfileName, long lastReading) {
        this.deviceId = deviceId;
        this.loadProfileName = loadProfileName;
        this.lastReading = lastReading;
    }
}
