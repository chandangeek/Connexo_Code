package com.energyict.mdc.device.data.api.impl;

import com.energyict.mdc.device.data.Device;
import java.util.List;
import java.util.stream.Collectors;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class DeviceInfo {
    public Links _links;
    public long id;
    public String mIRD;
    public String serialNumber;
    public String deviceTypeName;
    public Long deviceTypeId;
    public String deviceConfigurationName;
    public Long deviceConfigurationId;

    public DeviceInfo() {
    }

    public static DeviceInfo from(Device device) {
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.id = device.getId();
        deviceInfo.mIRD = device.getmRID();
        deviceInfo.serialNumber = device.getSerialNumber();
        deviceInfo.deviceTypeId = device.getDeviceType().getId();
        deviceInfo.deviceTypeName = device.getDeviceType().getName();
        deviceInfo.deviceConfigurationId = device.getDeviceConfiguration().getId();
        deviceInfo.deviceConfigurationName = device.getDeviceConfiguration().getName();
        return deviceInfo;
    }

    public static DeviceInfo fromHal(Device device) {
        DeviceInfo deviceInfo = from(device);
        deviceInfo._links = new Links("http://localhost:8085");
        return deviceInfo;
    }

    public static List<DeviceInfo> from(List<Device> devices) {
        return devices.stream().map(DeviceInfo::from).collect(Collectors.toList());
    }

    public static List<DeviceInfo> fromHal(List<Device> devices) {
        return devices.stream().map(DeviceInfo::from).collect(Collectors.toList());
    }
}

class Links {
    public Reference self;

    public Links(String href) {
        self=new Reference();
        self.href = href;
    }

    class Reference {
        public String href;
    }
}