package com.energyict.mdc.dashboard.rest.status.impl;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.rest.DeviceLabelInfo;
import com.energyict.mdc.favorites.DeviceLabel;

public class DeviceWithLabelInfo {

    public String name;
    public String serialNumber;
    public String deviceTypeName;
    public DeviceLabelInfo deviceLabelInfo;

    public DeviceWithLabelInfo(DeviceLabel deviceLabel) {
        Device device = deviceLabel.getDevice();
        this.name = device.getName();
        this.serialNumber = device.getSerialNumber();
        this.deviceTypeName = device.getDeviceType().getName();
        this.deviceLabelInfo = new DeviceLabelInfo(deviceLabel);
    }
}
