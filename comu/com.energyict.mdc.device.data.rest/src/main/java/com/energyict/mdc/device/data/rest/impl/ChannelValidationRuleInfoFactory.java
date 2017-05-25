/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.rest.ReadingTypeInfoFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.validation.ValidationPropertyDefinitionLevel;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.ValidationVersionStatus;
import com.energyict.mdc.device.data.ChannelValidationRuleOverriddenProperties;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ChannelValidationRuleInfoFactory {

    private final ValidationService validationService;
    private final PropertyValueInfoService propertyValueInfoService;
    private final ReadingTypeInfoFactory readingTypeInfoFactory;

    @Inject
    public ChannelValidationRuleInfoFactory(ValidationService validationService, PropertyValueInfoService propertyValueInfoService, ReadingTypeInfoFactory readingTypeInfoFactory) {
        this.validationService = validationService;
        this.propertyValueInfoService = propertyValueInfoService;
        this.readingTypeInfoFactory = readingTypeInfoFactory;
    }

    public ChannelValidationRuleInfo createInfoForRule(ValidationRule validationRule, ReadingType readingType, List<ValidationRuleSet> activeRuleSets, boolean validationActive) {
        ChannelValidationRuleInfo info = asInfo(validationRule, readingType, activeRuleSets, validationActive);
        setProperties(info, validationRule, Collections.emptyMap());
        return info;
    }

    public ChannelValidationRuleInfo createInfoForRule(ValidationRule validationRule, ReadingType readingType, ChannelValidationRuleOverriddenProperties overriddenProperties, List<ValidationRuleSet> activeRuleSets) {
        ChannelValidationRuleInfo info = asInfo(validationRule, readingType, activeRuleSets, overriddenProperties.getDevice().forValidation().isValidationActive());
        info.id = overriddenProperties.getId();
        info.version = overriddenProperties.getVersion();
        setProperties(info, validationRule, overriddenProperties.getProperties());
        return info;
    }

    private ChannelValidationRuleInfo asInfo(ValidationRule validationRule, ReadingType readingType, List<ValidationRuleSet> activeRuleSets, boolean validationActive) {
        ChannelValidationRuleInfo info = new ChannelValidationRuleInfo();
        boolean rulesetActive = validationActive && activeRuleSets.stream()
                .filter(validationRuleSet -> validationRule.getRuleSet().getId() == validationRuleSet.getId())
                .findAny()
                .isPresent();
        info.ruleId = validationRule.getId();
        info.name = validationRule.getName();
        info.validator = validationService.getValidator(validationRule.getImplementation()).getDisplayName();
        info.readingType = readingTypeInfoFactory.from(readingType);
        info.dataQualityLevel = ChannelValidationRuleInfo.DataQualityLevel.forValue(validationRule.getAction());
        info.isActive = validationRule.isActive() && rulesetActive;
        info.isEffective = ValidationVersionStatus.CURRENT == validationRule.getRuleSetVersion().getStatus();
        return info;
    }

    private void setProperties(ChannelValidationRuleInfo info, ValidationRule validationRule, Map<String, Object> overriddenProperties) {
        Map<String, PropertySpec> canBeOverriddenPropertySpecs = validationRule.getPropertySpecs(ValidationPropertyDefinitionLevel.TARGET_OBJECT)
                .stream().collect(Collectors.toMap(PropertySpec::getName, Function.identity()));
        info.properties = propertyValueInfoService.getPropertyInfos(validationRule.getPropertySpecs(), overriddenProperties, validationRule.getProps())
                .stream()
                .map(propertyInfo -> new OverriddenPropertyInfo(
                        propertyInfo,
                        canBeOverriddenPropertySpecs.containsKey(propertyInfo.key),
                        overriddenProperties.containsKey(propertyInfo.key)))
                .collect(Collectors.toList());
    }
}
