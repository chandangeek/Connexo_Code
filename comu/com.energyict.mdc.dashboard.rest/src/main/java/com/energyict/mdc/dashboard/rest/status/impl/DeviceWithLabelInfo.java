package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.favorites.DeviceLabel;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.rest.DeviceLabelInfo;

public class DeviceWithLabelInfo {
    
    public String mRID;
    public String serialNumber;
    public String deviceTypeName;
    public DeviceLabelInfo deviceLabelInfo;
    
    public DeviceWithLabelInfo() {
    }
    
    public static DeviceWithLabelInfo asInfo(DeviceLabel deviceLabel, DeviceService deviceService) {
        DeviceWithLabelInfo info = new DeviceWithLabelInfo();
        
        if (deviceLabel.getEndDevice().getAmrSystem().is(KnownAmrSystem.MDC)) {
            Device device = deviceService.findDeviceById(Long.parseLong(deviceLabel.getEndDevice().getAmrId()));
            if (device != null) {
                info.mRID = device.getmRID();
                info.serialNumber = device.getSerialNumber();
                info.deviceTypeName = device.getDeviceType().getName();
                info.deviceLabelInfo = new DeviceLabelInfo(deviceLabel);
            }
        }
        return info;
    }
}
