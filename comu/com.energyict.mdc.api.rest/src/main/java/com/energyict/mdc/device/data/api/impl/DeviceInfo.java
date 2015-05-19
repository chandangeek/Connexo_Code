package com.energyict.mdc.device.data.api.impl;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class DeviceInfo extends LinkInfo {
    public Long id;
    public String mIRD;
    public String serialNumber;
    public String name;
    public DeviceConfigurationInfo deviceConfiguration;

    public DeviceInfo() {
    }

}

