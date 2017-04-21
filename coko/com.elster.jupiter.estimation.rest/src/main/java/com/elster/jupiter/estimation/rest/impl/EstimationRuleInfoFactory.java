/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation.rest.impl;

import com.elster.jupiter.estimation.EstimationPropertyDefinitionLevel;
import com.elster.jupiter.estimation.EstimationRule;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.metering.rest.ReadingTypeInfoFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class EstimationRuleInfoFactory {

    private final PropertyValueInfoService propertyValueInfoService;
    private final ReadingTypeInfoFactory readingTypeInfoFactory;
    private final Provider<EstimationRuleSetInfoFactory> estimationRuleSetInfoFactoryProvider;

    @Inject
    EstimationRuleInfoFactory(PropertyValueInfoService propertyValueInfoService, ReadingTypeInfoFactory readingTypeInfoFactory,
                              Provider<EstimationRuleSetInfoFactory> estimationRuleSetInfoFactoryProvider) {
        this.propertyValueInfoService = propertyValueInfoService;
        this.readingTypeInfoFactory = readingTypeInfoFactory;
        this.estimationRuleSetInfoFactoryProvider = estimationRuleSetInfoFactoryProvider;
    }

    EstimationRuleInfo asInfo(EstimationRule estimationRule) {
        EstimationRuleInfo estimationRuleInfo = new EstimationRuleInfo();
        estimationRuleInfo.id = estimationRule.getId();
        estimationRuleInfo.name = estimationRule.getName();
        estimationRuleInfo.version = estimationRule.getVersion();
        estimationRuleInfo.implementation = estimationRule.getImplementation();
        estimationRuleInfo.displayName = estimationRule.getDisplayName();
        estimationRuleInfo.active = estimationRule.isActive();
        estimationRuleInfo.deleted = estimationRule.isObsolete();
        List<PropertySpec> propertySpecs = estimationRule.getPropertySpecs(EstimationPropertyDefinitionLevel.ESTIMATION_RULE);
        Map<String, Object> actualPropertyValues = estimationRule.getProps();
        estimationRuleInfo.properties = propertyValueInfoService.getPropertyInfos(propertySpecs, actualPropertyValues);
        estimationRuleInfo.readingTypes.addAll(estimationRule.getReadingTypes().stream().map(readingTypeInfoFactory::from).collect(Collectors.toList()));
        EstimationRuleSet ruleSet = estimationRule.getRuleSet();
        estimationRuleInfo.parent.id = ruleSet.getId();
        estimationRuleInfo.parent.version = ruleSet.getVersion();
        estimationRuleInfo.ruleSet = estimationRuleSetInfoFactoryProvider.get().asInfo(estimationRule.getRuleSet());
        return estimationRuleInfo;
    }
}
