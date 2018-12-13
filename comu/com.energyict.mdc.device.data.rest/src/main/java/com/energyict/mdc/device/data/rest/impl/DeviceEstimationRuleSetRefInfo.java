/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.rest.util.VersionInfo;
import com.energyict.mdc.device.configuration.rest.EstimationRuleSetRefInfo;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceEstimationRuleSetActivation;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class DeviceEstimationRuleSetRefInfo extends EstimationRuleSetRefInfo {

    public boolean active;
    public VersionInfo<String> parent;
    
    public DeviceEstimationRuleSetRefInfo() {
    }
    
    public DeviceEstimationRuleSetRefInfo(DeviceEstimationRuleSetActivation estimationRuleSetActivation, Device device) {
        super(estimationRuleSetActivation.getEstimationRuleSet());
        this.active = estimationRuleSetActivation.isActive();
        this.parent = new VersionInfo<>(device.getName(), device.getVersion());
    }
}
