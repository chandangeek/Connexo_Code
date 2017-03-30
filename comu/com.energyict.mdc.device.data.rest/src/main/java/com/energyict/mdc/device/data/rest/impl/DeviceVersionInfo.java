/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.rest.util.VersionInfo;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.data.Device;

import java.util.List;
import java.util.stream.Collectors;

public class DeviceVersionInfo {

    public String name;
    public long version;
    public VersionInfo<Long> parent;

    public static DeviceVersionInfo from(Device device) {
        DeviceVersionInfo deviceVersionInfo = new DeviceVersionInfo();
        deviceVersionInfo.version = device.getVersion();
        deviceVersionInfo.name = device.getName();
        DeviceConfiguration deviceConfiguration = device.getDeviceConfiguration();
        deviceVersionInfo.parent = new VersionInfo<>(deviceConfiguration.getId(), deviceConfiguration.getVersion());
        return deviceVersionInfo;
    }

    public static List<DeviceVersionInfo> fromDevices(List<Device> devices) {
        return devices.stream().map(device -> DeviceVersionInfo.from(device)).collect(Collectors.toList());
    }
}
