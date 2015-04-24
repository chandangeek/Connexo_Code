package com.energyict.mdc.device.configuration.rest.impl;

import javax.xml.bind.annotation.XmlRootElement;

import com.elster.jupiter.estimation.EstimationRuleSet;
import com.energyict.mdc.device.config.DeviceConfiguration;

@XmlRootElement
public class EstimationRuleSetRefInfo {

    public long id;
    public String name;
    public int numberOfInactiveRules = 0;
    public int numberOfRules = 0;
    public EntityRefInfo parent;

    public static EstimationRuleSetRefInfo from(EstimationRuleSet estimationRuleSet, DeviceConfiguration deviceConfiguration) {
        EstimationRuleSetRefInfo info = new EstimationRuleSetRefInfo();
        info.id = estimationRuleSet.getId();
        info.name = estimationRuleSet.getName();
        info.numberOfRules = estimationRuleSet.getRules().size();
        info.numberOfInactiveRules = (int) estimationRuleSet.getRules().stream().filter(rule -> !rule.isActive()).count();
        info.parent = new EntityRefInfo(deviceConfiguration.getId(), deviceConfiguration.getVersion());
        return info;
    }
}
