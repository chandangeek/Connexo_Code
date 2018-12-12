/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation.rest.impl;

import com.elster.jupiter.estimation.EstimationRule;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.estimation.EstimationService;

import javax.inject.Inject;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.streams.Predicates.not;

class EstimationRuleSetInfoFactory {

    private final EstimationService estimationService;
    private final EstimationRuleInfoFactory estimationRuleInfoFactory;

    @Inject
    EstimationRuleSetInfoFactory(EstimationService estimationService, EstimationRuleInfoFactory estimationRuleInfoFactory) {
        this.estimationService = estimationService;
        this.estimationRuleInfoFactory = estimationRuleInfoFactory;
    }

    EstimationRuleSetInfo asInfo(EstimationRuleSet estimationRuleSet) {
        EstimationRuleSetInfo info = new EstimationRuleSetInfo();
        info.id = estimationRuleSet.getId();
        info.name = estimationRuleSet.getName();
        info.description = estimationRuleSet.getDescription();
        info.numberOfRules = estimationRuleSet.getRules().size();
        info.numberOfInactiveRules = estimationRuleSet.getRules().stream().filter(not(EstimationRule::isActive)).count();
        info.version = estimationRuleSet.getVersion();
        return info;
    }

    EstimationRuleSetInfo asFullInfo(EstimationRuleSet estimationRuleSet) {
        EstimationRuleSetInfo info = asInfo(estimationRuleSet);
        info.isInUse = estimationService.isEstimationRuleSetInUse(estimationRuleSet);
        info.rules = estimationRuleSet.getRules().stream()
                .map(estimationRuleInfoFactory::asInfo)
                .collect(Collectors.toList());
        return info;
    }
}
