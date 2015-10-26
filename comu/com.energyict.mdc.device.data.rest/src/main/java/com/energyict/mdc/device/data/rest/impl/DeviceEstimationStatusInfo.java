package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.device.data.Device;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class DeviceEstimationStatusInfo {

    public boolean active;
    
    public DeviceEstimationStatusInfo() {
    }
    
    public DeviceEstimationStatusInfo(Device device) {
        this.active = device.forEstimation().isEstimationActive();
    }
    
}
