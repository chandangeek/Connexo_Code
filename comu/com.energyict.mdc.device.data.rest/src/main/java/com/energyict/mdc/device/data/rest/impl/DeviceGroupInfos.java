package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceService;

import java.util.ArrayList;
import java.util.List;

public class DeviceGroupInfos {

    public int total;
    public List<DeviceGroupInfo> deviceGroups = new ArrayList<>();

    public DeviceGroupInfos() {
    }

    public DeviceGroupInfos(Iterable<? extends EndDeviceGroup> deviceGroups, DeviceConfigurationService deviceConfigurationService, DeviceService deviceService) {
        addAll(deviceGroups, deviceConfigurationService, deviceService);
    }

    public DeviceGroupInfo add(EndDeviceGroup endDeviceGroup, DeviceConfigurationService deviceConfigurationService, DeviceService deviceService) {
        DeviceGroupInfo result = DeviceGroupInfo.from(endDeviceGroup, deviceConfigurationService, deviceService);
        deviceGroups.add(result);
        total++;
        return result;
    }

    public void addAll(Iterable<? extends EndDeviceGroup> deviceGroups, DeviceConfigurationService deviceConfigurationService, DeviceService deviceService) {
        for (EndDeviceGroup each : deviceGroups) {
            add(each, deviceConfigurationService, deviceService);
        }
    }
}
