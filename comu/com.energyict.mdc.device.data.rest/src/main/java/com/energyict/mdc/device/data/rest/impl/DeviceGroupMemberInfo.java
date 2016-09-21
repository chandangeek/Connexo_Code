package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.energyict.mdc.device.data.Device;

import java.util.List;
import java.util.stream.Collectors;

public class DeviceGroupMemberInfo {

    public long id;
    public String name;
    public String serialNumber;
    public String deviceTypeName;
    public String deviceConfigurationName;
    
    public static DeviceGroupMemberInfo from(Device device, EndDeviceGroup endDeviceGroup) {
        DeviceGroupMemberInfo deviceInfo = new DeviceGroupMemberInfo();
        deviceInfo.id = device.getId();
        deviceInfo.name = device.getName();
        deviceInfo.serialNumber = device.getSerialNumber();
        deviceInfo.deviceTypeName = device.getDeviceType().getName();
        deviceInfo.deviceConfigurationName = device.getDeviceConfiguration().getName();
        return deviceInfo;
    }
    
    public static List<DeviceGroupMemberInfo> from(List<Device> devices, EndDeviceGroup endDeviceGroup, AmrSystem amrSystem) {
        return devices.stream().map((d) -> from(d, endDeviceGroup)).collect(Collectors.toList());
    }

    public static DeviceGroupMemberInfo from(Device device) {
        DeviceGroupMemberInfo deviceInfo = new DeviceGroupMemberInfo();
        deviceInfo.id = device.getId();
        deviceInfo.name = device.getName();
        deviceInfo.serialNumber = device.getSerialNumber();
        deviceInfo.deviceTypeName = device.getDeviceType().getName();
        deviceInfo.deviceConfigurationName = device.getDeviceConfiguration().getName();
        return deviceInfo;
    }
    
    public static List<DeviceGroupMemberInfo> from(List<Device> devices) {
        return devices.stream().map(DeviceGroupMemberInfo::from).collect(Collectors.toList());
    }
}
