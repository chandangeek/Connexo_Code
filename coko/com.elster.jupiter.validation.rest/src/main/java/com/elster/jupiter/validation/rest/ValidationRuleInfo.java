package com.elster.jupiter.validation.rest;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.elster.jupiter.validation.DataValidationStatus;
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
    public List<PropertyInfo> properties = new ArrayList<PropertyInfo>();
    public List<ReadingTypeInfo> readingTypes = new ArrayList<ReadingTypeInfo>();
    public ValidationRuleSetInfo ruleSet;

    public ValidationRuleInfo(ValidationRule validationRule) {
        id = validationRule.getId();
        active = validationRule.isActive();
        implementation = validationRule.getImplementation();
        displayName = validationRule.getDisplayName();
        name = validationRule.getName();
        deleted = validationRule.isObsolete();
        ruleSet = new ValidationRuleSetInfo(validationRule.getRuleSet());
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
            if(validationRule.isObsolete()) {
                validationRuleInfos.add(getObsoleteRuleInfo());
            } else {
                validationRuleInfos.add(new ValidationRuleInfo(validationRule));
            }
        }
        return validationRuleInfos;
    }

    public static Map<ValidationRuleInfo, Long> from(Map<ValidationRule, Long> suspectReasonMap) {
        Map<ValidationRuleInfo, Long> suspectReasonInfoMap = new HashMap<>();
        for(Map.Entry<ValidationRule, Long> entry : suspectReasonMap.entrySet()) {
            if(entry.getKey().isObsolete()) {
                putObsoleteRuleInfo(suspectReasonInfoMap, entry);
            } else {
                suspectReasonInfoMap.put(new ValidationRuleInfo(entry.getKey()), entry.getValue());
            }
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

    private static void putObsoleteRuleInfo(Map<ValidationRuleInfo, Long> suspectReasonInfoMap, Map.Entry<ValidationRule, Long> entry) {
        ValidationRuleInfo validationRuleInfo = getObsoleteRuleInfo();
        if(suspectReasonInfoMap.containsKey(validationRuleInfo)) {
            Long number = suspectReasonInfoMap.get(validationRuleInfo);
            suspectReasonInfoMap.put(validationRuleInfo, ++number);
        } else {
            suspectReasonInfoMap.put(validationRuleInfo, 1L);
        }
    }

    private static ValidationRuleInfo getObsoleteRuleInfo() {
        ValidationRuleInfo validationRuleInfo = new ValidationRuleInfo();
        validationRuleInfo.id = 0;
        validationRuleInfo.name = "removed rule";
        return validationRuleInfo;
    }
}
