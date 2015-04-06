package com.elster.jupiter.validation.rest;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.ValidationRule;

import java.util.*;

public class ValidationRuleInfo {

    public long id;
    public boolean active;
    public boolean deleted;
    public String implementation; //validator classname
    public String displayName; // readable name
    public String name;
    public int position;
    public ValidationAction action = ValidationAction.FAIL;
    public List<PropertyInfo> properties = new ArrayList<PropertyInfo>();
    public List<ReadingTypeInfo> readingTypes = new ArrayList<ReadingTypeInfo>();
    public ValidationRuleSetVersionInfo ruleSetVersion;

    public ValidationRuleInfo(ValidationRule validationRule) {
        id = validationRule.getId();
        active = validationRule.isActive();
        implementation = validationRule.getImplementation();
        displayName = validationRule.getDisplayName();
        action = validationRule.getAction();
        name = validationRule.getName();
        deleted = validationRule.isObsolete();
        ruleSetVersion = new ValidationRuleSetVersionInfo(validationRule.getRuleSetVersion());
        properties = new PropertyUtils().convertPropertySpecsToPropertyInfos(validationRule.getPropertySpecs(), validationRule.getProps());
        for (ReadingType readingType : validationRule.getReadingTypes()) {
            readingTypes.add(new ReadingTypeInfo(readingType));
        }
    }

    public static List<ValidationRuleInfo> from(List<ValidationRule> validationRules) {
        List<ValidationRuleInfo> infos = new ArrayList<>(validationRules.size());
        for (ValidationRule validationRule : validationRules) {
            infos.add(new ValidationRuleInfo(validationRule));
        }
        return infos;
    }

    public static Set<ValidationRuleInfo> from(DataValidationStatus dataValidationStatus) {
        Set<ValidationRuleInfo> validationRuleInfos = new LinkedHashSet<>();
        for (ValidationRule validationRule : dataValidationStatus.getOffendedRules()) {
            validationRuleInfos.add(new ValidationRuleInfo(validationRule));
        }
        return validationRuleInfos;
    }

    public static Map<ValidationRuleInfo, Long> from(Map<ValidationRule, Long> suspectReasonMap) {
        Map<ValidationRuleInfo, Long> suspectReasonInfoMap = new HashMap<>();
        for (Map.Entry<ValidationRule, Long> entry : suspectReasonMap.entrySet()) {
            suspectReasonInfoMap.put(new ValidationRuleInfo(entry.getKey()), entry.getValue());
        }
        return suspectReasonInfoMap;
    }

    public ValidationRuleInfo() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return id == ((ValidationRuleInfo) o).id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
