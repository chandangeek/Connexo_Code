package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.GatewayReference;

import java.util.List;
import java.util.stream.Collectors;

public class DeviceTopologyInfo {
    public long id;
    public String mRID;
    public String deviceTypeName;
    public String deviceConfigurationName;
    public long creationTime;

    public static List<DeviceTopologyInfo> from(List<? extends GatewayReference> references) {
        return references.stream().map(DeviceTopologyInfo::from).collect(Collectors.toList());
    }

    public static List<DeviceTopologyInfo> fromDevices(List<Device> devices) {
        return devices.stream().map(DeviceTopologyInfo::from).collect(Collectors.toList());
    }

    public static DeviceTopologyInfo from(GatewayReference reference) {
        DeviceTopologyInfo info = new DeviceTopologyInfo();
        info.id = reference.getOrigin().getId();
        info.mRID = reference.getOrigin().getmRID();
        info.deviceTypeName = reference.getOrigin().getDeviceType().getName();
        info.deviceConfigurationName = reference.getOrigin().getDeviceConfiguration().getName();
        info.creationTime = reference.getInterval().getStart().toEpochMilli();
        return info;
    }

    public static DeviceTopologyInfo from(Device device) {
        DeviceTopologyInfo info = new DeviceTopologyInfo();
        info.id = device.getId();
        info.mRID = device.getmRID();
        return info;
    }
}
