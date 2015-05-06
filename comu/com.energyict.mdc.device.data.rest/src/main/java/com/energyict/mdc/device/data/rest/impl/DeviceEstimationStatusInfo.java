package com.energyict.mdc.device.data.rest.impl;

import javax.xml.bind.annotation.XmlRootElement;

import com.energyict.mdc.device.data.Device;

@XmlRootElement
public class DeviceEstimationStatusInfo {

    public boolean active;
    
    public DeviceEstimationStatusInfo() {
    }
    
    public DeviceEstimationStatusInfo(Device device) {
        this.active = device.forEstimation().isEstimationActive();
    }
    
}
