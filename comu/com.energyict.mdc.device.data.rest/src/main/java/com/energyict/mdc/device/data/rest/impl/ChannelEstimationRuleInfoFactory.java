/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.estimation.EstimationPropertyDefinitionLevel;
import com.elster.jupiter.estimation.EstimationRule;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.rest.ReadingTypeInfoFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.energyict.mdc.device.data.ChannelEstimationRuleOverriddenProperties;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ChannelEstimationRuleInfoFactory {

    private final EstimationService estimationService;
    private final PropertyValueInfoService propertyValueInfoService;
    private final ReadingTypeInfoFactory readingTypeInfoFactory;

    @Inject
    public ChannelEstimationRuleInfoFactory(EstimationService estimationService, PropertyValueInfoService propertyValueInfoService, ReadingTypeInfoFactory readingTypeInfoFactory) {
        this.estimationService = estimationService;
        this.propertyValueInfoService = propertyValueInfoService;
        this.readingTypeInfoFactory = readingTypeInfoFactory;
    }

    public ChannelEstimationRuleInfo createInfoForRule(EstimationRule estimationRule, ReadingType readingType) {
        ChannelEstimationRuleInfo info = asInfo(estimationRule, readingType);
        setProperties(info, estimationRule, Collections.emptyMap());
        return info;
    }

    public ChannelEstimationRuleInfo createInfoForRule(EstimationRule estimationRule, ReadingType readingType, ChannelEstimationRuleOverriddenProperties overriddenProperties) {
        ChannelEstimationRuleInfo info = asInfo(estimationRule, readingType);
        info.id = overriddenProperties.getId();
        info.version = overriddenProperties.getVersion();
        setProperties(info, estimationRule, overriddenProperties.getProperties());
        return info;
    }

    private ChannelEstimationRuleInfo asInfo(EstimationRule estimationRule, ReadingType readingType) {
        ChannelEstimationRuleInfo info = new ChannelEstimationRuleInfo();
        info.ruleId = estimationRule.getId();
        info.name = estimationRule.getName();
        info.estimator = estimationService.getEstimator(estimationRule.getImplementation()).get().getDisplayName();
        info.readingType = readingTypeInfoFactory.from(readingType);
        info.isActive = estimationRule.isActive();
        return info;
    }

    private void setProperties(ChannelEstimationRuleInfo info, EstimationRule estimationRule, Map<String, Object> overriddenProperties) {
        Map<String, PropertySpec> canBeOverriddenPropertySpecs = estimationRule.getPropertySpecs(EstimationPropertyDefinitionLevel.TARGET_OBJECT)
                .stream().collect(Collectors.toMap(PropertySpec::getName, Function.identity()));
        info.properties = propertyValueInfoService.getPropertyInfos(estimationRule.getPropertySpecs(), overriddenProperties, estimationRule.getProps())
                .stream()
                .map(propertyInfo -> new OverriddenPropertyInfo(
                        propertyInfo,
                        canBeOverriddenPropertySpecs.containsKey(propertyInfo.key),
                        overriddenProperties.containsKey(propertyInfo.key)))
                .collect(Collectors.toList());
    }
}
