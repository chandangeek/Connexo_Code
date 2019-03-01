package com.energyict.mdc.device.data.rest.impl;

import java.util.List;

public class DeviceZoneInfo {

    public int total;
    public List<String> deviceNames;

    public static DeviceZoneInfo from(int total ,List<String> deviceNames) {
        DeviceZoneInfo deviceZoneInfo = new DeviceZoneInfo();
        deviceZoneInfo.total = total;
        deviceZoneInfo.deviceNames = deviceNames;

        return deviceZoneInfo;
    }
}