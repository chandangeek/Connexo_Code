package com.energyict.mdc.device.configuration.rest;

import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.rest.util.VersionInfo;
import com.energyict.mdc.device.config.DeviceConfiguration;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class EstimationRuleSetRefInfo {

    public long id;
    public String name;
    public int numberOfInactiveRules = 0;
    public int numberOfRules = 0;
    public long version;
    public VersionInfo<Long> parent;
    
    public EstimationRuleSetRefInfo() {
    }

    public EstimationRuleSetRefInfo(EstimationRuleSet estimationRuleSet, DeviceConfiguration deviceConfiguration) {
        this(estimationRuleSet);
        this.parent = new VersionInfo<>(deviceConfiguration.getId(), deviceConfiguration.getVersion());
    }
    
    public EstimationRuleSetRefInfo(EstimationRuleSet estimationRuleSet) {
        this.id = estimationRuleSet.getId();
        this.name = estimationRuleSet.getName();
        this.numberOfRules = estimationRuleSet.getRules().size();
        this.numberOfInactiveRules = (int) estimationRuleSet.getRules().stream().filter(rule -> !rule.isActive()).count();
        this.version = estimationRuleSet.getVersion();
    }
}
