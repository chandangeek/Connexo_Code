package com.elster.jupiter.mdm.usagepoint.config.rest.impl;

import com.elster.jupiter.estimation.EstimationRule;
import com.elster.jupiter.estimation.EstimationRuleSet;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class EstimationRuleSetInfo {
    public long id;
    public String name;
    public String description;
    public long activeRules;
    public long inactiveRules;

    public EstimationRuleSetInfo(EstimationRuleSet estimationRuleSet) {
        id = estimationRuleSet.getId();
        name = estimationRuleSet.getName();
        description = estimationRuleSet.getDescription();
        activeRules = estimationRuleSet.getRules().stream().filter(EstimationRule::isActive).count();
        inactiveRules = estimationRuleSet.getRules().size() - activeRules;
    }

    public EstimationRuleSetInfo() {
    }
}
