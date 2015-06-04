package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.device.config.DeviceConfiguration;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class DeviceConfigurationRefInfo {

    public long id;
    public String name;
    public Boolean active;
    public Integer loadProfileCount;
    public Integer registerCount;
    public long deviceTypeId;
    public String deviceTypeName;
    public long version;
    
    public static DeviceConfigurationRefInfo from(DeviceConfiguration deviceConfiguration) {
        DeviceConfigurationRefInfo info = new DeviceConfigurationRefInfo();
        info.id = deviceConfiguration.getId();
        info.name = deviceConfiguration.getName();
        info.active = deviceConfiguration.isActive();
        info.loadProfileCount = deviceConfiguration.getLoadProfileSpecs().size();
        info.registerCount = deviceConfiguration.getRegisterSpecs().size();
        info.deviceTypeId = deviceConfiguration.getDeviceType().getId();
        info.deviceTypeName = deviceConfiguration.getDeviceType().getName();
        info.version = deviceConfiguration.getVersion();
        return info;
    }
}
