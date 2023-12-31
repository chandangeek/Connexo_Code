/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.config.rest;

import com.elster.jupiter.estimation.EstimationRule;
import com.elster.jupiter.estimation.EstimationRuleSet;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Comparator;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class EstimationRuleSetInfo {

    public static Comparator<EstimationRuleSetInfo> ESTIMATION_RULESET_NAME_COMPARATOR = Comparator.comparing(info -> info.name.toLowerCase());

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
