package com.elster.jupiter.validation.rest;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.properties.PropertyValueInfoService;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.rest.util.VersionInfo;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSetVersion;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Copyrights EnergyICT
 * Date: 28/05/15
 * Time: 10:09
 */
public class ValidationRuleInfoFactory {

    private final PropertyValueInfoService propertyValueInfoService;

    @Inject
    public ValidationRuleInfoFactory(PropertyValueInfoService propertyValueInfoService) {
        this.propertyValueInfoService = propertyValueInfoService;
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
        ValidationRuleSetVersion ruleSetVersion = validationRule.getRuleSetVersion();
        validationRuleInfo.ruleSetVersion = new ValidationRuleSetVersionInfo(ruleSetVersion);
        validationRuleInfo.properties = propertyValueInfoService.getPropertyInfos(validationRule.getPropertySpecs(), validationRule.getProps());
        validationRuleInfo.readingTypes.addAll(validationRule.getReadingTypes().stream().map(ReadingTypeInfo::new).collect(Collectors.toList()));
        validationRuleInfo.version = validationRule.getVersion();
        validationRuleInfo.parent = new VersionInfo<>(ruleSetVersion.getId(), ruleSetVersion.getVersion());
        if (validationRule.getRuleSet().getQualityCodeSystem() != null) {
            IdWithNameInfo applicationInfo = new IdWithNameInfo();
            applicationInfo.id = validationRule.getRuleSet().getQualityCodeSystem().name();
            applicationInfo.name = validationRule.getRuleSet().getQualityCodeSystem() == QualityCodeSystem.MDC ? "MultiSense" : "Insight";
            validationRuleInfo.application = applicationInfo;
        }
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
