package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.groups.EndDeviceGroup;

import java.util.ArrayList;
import java.util.List;

public class DeviceGroupInfos {

    public int total;
    public List<DeviceGroupInfo> deviceGroups = new ArrayList<>();

    public DeviceGroupInfos() {
    }

    public DeviceGroupInfos(Iterable<? extends EndDeviceGroup> deviceGroups) {
        addAll(deviceGroups);
    }

    public DeviceGroupInfo add(EndDeviceGroup endDeviceGroup) {
        DeviceGroupInfo result = DeviceGroupInfo.from(endDeviceGroup);
        deviceGroups.add(result);
        total++;
        return result;
    }

    public void addAll(Iterable<? extends EndDeviceGroup> deviceGroups) {
        for (EndDeviceGroup each : deviceGroups) {
            add(each);
        }
    }
}
