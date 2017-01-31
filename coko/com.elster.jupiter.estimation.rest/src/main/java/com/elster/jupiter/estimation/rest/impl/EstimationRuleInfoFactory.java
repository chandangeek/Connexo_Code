/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation.rest.impl;

import com.elster.jupiter.estimation.EstimationRule;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.metering.rest.ReadingTypeInfoFactory;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;

import javax.inject.Inject;
import java.util.stream.Collectors;

public class EstimationRuleInfoFactory {

    private final PropertyValueInfoService propertyValueInfoService;
    private final ReadingTypeInfoFactory readingTypeInfoFactory;

    @Inject
    public EstimationRuleInfoFactory(PropertyValueInfoService propertyValueInfoService,
                                     ReadingTypeInfoFactory readingTypeInfoFactory) {
        this.propertyValueInfoService = propertyValueInfoService;
        this.readingTypeInfoFactory = readingTypeInfoFactory;
    }

    public EstimationRuleInfo createEstimationRuleInfo(EstimationRule estimationRule) {
        EstimationRuleInfo estimationRuleInfo = new EstimationRuleInfo();

        estimationRuleInfo.id = estimationRule.getId();
        estimationRuleInfo.active = estimationRule.isActive();
        estimationRuleInfo.implementation = estimationRule.getImplementation();
        estimationRuleInfo.displayName = estimationRule.getDisplayName();
        estimationRuleInfo.name = estimationRule.getName();
        estimationRuleInfo.deleted = estimationRule.isObsolete();
        EstimationRuleSet ruleSet = estimationRule.getRuleSet();
        estimationRuleInfo.ruleSet = new EstimationRuleSetInfo(ruleSet);
        estimationRuleInfo.properties = propertyValueInfoService.getPropertyInfos(estimationRule.getPropertySpecs(), estimationRule.getProps());
        estimationRuleInfo.readingTypes.addAll(estimationRule.getReadingTypes().stream().map(readingTypeInfoFactory::from).collect(Collectors.toList()));
        estimationRuleInfo.version = estimationRule.getVersion();
        estimationRuleInfo.parent.id = ruleSet.getId();
        estimationRuleInfo.parent.version = ruleSet.getVersion();

        return estimationRuleInfo;
    }
}
