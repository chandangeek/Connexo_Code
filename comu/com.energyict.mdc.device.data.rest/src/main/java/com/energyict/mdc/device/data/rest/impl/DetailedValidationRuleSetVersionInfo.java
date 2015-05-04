package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSetVersion;
import com.elster.jupiter.validation.rest.ValidationRuleSetVersionInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by adrianlupan on 4/22/15.
 */
public class DetailedValidationRuleSetVersionInfo extends ValidationRuleSetVersionInfo {

    public long total;
    public List<DetailedValidationRuleInfo> detailedRules;


    public DetailedValidationRuleSetVersionInfo(ValidationRuleSetVersion version, Long total, Map<ValidationRule, Long> suspectReasonMap) {
        super(version);
        this.total = total;
        Map<ValidationRule, Long> rules = suspectReasonMap.entrySet().stream()
                .filter(rule -> rule.getKey().getRuleSetVersion().getId() == version.getId())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        this.detailedRules = computeRules(rules);

    }

    public DetailedValidationRuleSetVersionInfo() {

    }

    private List<DetailedValidationRuleInfo> computeRules(Map<ValidationRule, Long> suspectReasonMap) {
        List<DetailedValidationRuleInfo> result = new ArrayList<>();
        suspectReasonMap.entrySet().stream().forEach(rule -> {
            result.add(new DetailedValidationRuleInfo(rule.getKey(),rule.getValue()));
        });
        return result;
    }
}
