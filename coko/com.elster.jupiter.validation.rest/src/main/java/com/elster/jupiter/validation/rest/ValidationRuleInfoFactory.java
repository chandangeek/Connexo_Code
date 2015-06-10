package com.elster.jupiter.validation.rest;

import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationRule;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Copyrights EnergyICT
 * Date: 28/05/15
 * Time: 10:09
 */
public class ValidationRuleInfoFactory {

    private final PropertyUtils propertyUtils;

    @Inject
    public ValidationRuleInfoFactory(PropertyUtils propertyUtils) {
        this.propertyUtils = propertyUtils;
    }

    public ValidationRuleInfo createValidationRuleInfo(ValidationRule validationRule) {
        ValidationRuleInfo validationRuleInfo = new ValidationRuleInfo();
        validationRuleInfo.id = validationRule.getId();
        validationRuleInfo.active = validationRule.isActive();
        validationRuleInfo.implementation = validationRule.getImplementation();
        validationRuleInfo.displayName = validationRule.getDisplayName();
        validationRuleInfo.action = validationRule.getAction();
        validationRuleInfo.name = validationRule.getName();
        validationRuleInfo.deleted = validationRule.isObsolete();
        validationRuleInfo.ruleSetVersion = new ValidationRuleSetVersionInfo(validationRule.getRuleSetVersion());
        validationRuleInfo.properties = propertyUtils.convertPropertySpecsToPropertyInfos(validationRule.getPropertySpecs(), validationRule.getProps());
        validationRuleInfo.readingTypes.addAll(validationRule.getReadingTypes().stream().map(ReadingTypeInfo::new).collect(Collectors.toList()));
        return validationRuleInfo;
    }

    public ValidationRuleInfos createValidationRuleInfos(List<ValidationRuleInfo> validationRules) {
        ValidationRuleInfos validationRuleInfos = new ValidationRuleInfos();
        validationRuleInfos.rules = validationRules;
        validationRuleInfos.total = validationRules.size();
        return validationRuleInfos;
    }

    public Set<ValidationRuleInfo> createInfosForDataValidationStatus(DataValidationStatus dataValidationStatus) {
        return dataValidationStatus.getOffendedRules().stream().map(this::createValidationRuleInfo).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Set<ValidationRuleInfo> createInfosForBulkDataValidationStatus(DataValidationStatus dataValidationStatus) {
        return dataValidationStatus.getBulkOffendedRules().stream().map(this::createValidationRuleInfo).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Map<ValidationRuleInfo, Long> createInfosForSuspectReasons(Map<ValidationRule, Long> suspectReasonMap){
        Map<ValidationRuleInfo, Long> suspectReasonInfoMap = new HashMap<>();
        for (Map.Entry<ValidationRule, Long> entry : suspectReasonMap.entrySet()) {
            suspectReasonInfoMap.put(createValidationRuleInfo(entry.getKey()), entry.getValue());
        }
        return suspectReasonInfoMap;
    }
}
