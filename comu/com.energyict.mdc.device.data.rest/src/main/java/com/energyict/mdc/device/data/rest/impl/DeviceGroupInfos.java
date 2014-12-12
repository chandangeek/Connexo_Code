package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.energyict.mdc.device.config.DeviceConfigurationService;

import java.util.ArrayList;
import java.util.List;

public class DeviceGroupInfos {

    public int total;
    public List<DeviceGroupInfo> deviceGroups = new ArrayList<>();

    public DeviceGroupInfos() {
    }

    public DeviceGroupInfos(Iterable<? extends EndDeviceGroup> deviceGroups, DeviceConfigurationService deviceConfigurationService) {
        addAll(deviceGroups, deviceConfigurationService);
    }

    public DeviceGroupInfo add(EndDeviceGroup endDeviceGroup, DeviceConfigurationService deviceConfigurationService) {
        DeviceGroupInfo result = DeviceGroupInfo.from(endDeviceGroup, deviceConfigurationService);
        deviceGroups.add(result);
        total++;
        return result;
    }

    public void addAll(Iterable<? extends EndDeviceGroup> deviceGroups, DeviceConfigurationService deviceConfigurationService) {
        for (EndDeviceGroup each : deviceGroups) {
            add(each, deviceConfigurationService);
        }
    }
}
