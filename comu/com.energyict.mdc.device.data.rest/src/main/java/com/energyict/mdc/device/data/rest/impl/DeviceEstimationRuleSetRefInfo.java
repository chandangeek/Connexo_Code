package com.energyict.mdc.device.data.rest.impl;

import javax.xml.bind.annotation.XmlRootElement;

import com.energyict.mdc.device.configuration.rest.EntityRefInfo;
import com.energyict.mdc.device.configuration.rest.EstimationRuleSetRefInfo;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceEstimationRuleSetActivation;

@XmlRootElement
public class DeviceEstimationRuleSetRefInfo extends EstimationRuleSetRefInfo {

    public boolean active;
    
    public DeviceEstimationRuleSetRefInfo() {
    }
    
    public DeviceEstimationRuleSetRefInfo(DeviceEstimationRuleSetActivation estimationRuleSetActivation, Device device) {
        super(estimationRuleSetActivation.getEstimationRuleSet());
        this.active = estimationRuleSetActivation.isActive();
        this.parent = new EntityRefInfo(device.getId(), device.getVersion());
    }
}
