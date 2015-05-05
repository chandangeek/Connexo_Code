package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationRuleSetVersion;
import com.elster.jupiter.validation.rest.ValidationRuleSetInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by adrianlupan on 4/22/15.
 */
public class DetailedValidationRuleSetInfo extends ValidationRuleSetInfo{

    public long id;
    public long total;
    public String name;
    public List<DetailedValidationRuleSetVersionInfo> detailedRuleSetVersions;


    public DetailedValidationRuleSetInfo(ValidationRuleSet ruleSet, Long total, Map<ValidationRuleSetVersion, Long> suspectReasonRuleSetVersionMap, Map<ValidationRule, Long> suspectReasonMap) {
        this.id = ruleSet.getId();
        this.name = ruleSet.getName();
        this.total = total;
        Map<ValidationRuleSetVersion, Long> versions = suspectReasonRuleSetVersionMap.entrySet().stream()
                .filter(version -> version.getKey().getRuleSet().getId() == ruleSet.getId())
                .collect(Collectors.toMap(Map.Entry::getKey,Map.Entry::getValue ));
        this.detailedRuleSetVersions = computeRuleSetVersionChain(versions, suspectReasonMap);

    }

    public DetailedValidationRuleSetInfo() {

    }

    private List<DetailedValidationRuleSetVersionInfo> computeRuleSetVersionChain(Map<ValidationRuleSetVersion, Long> suspectReasonRuleSetVersionMap, Map<ValidationRule, Long> suspectReasonMap) {
        List<DetailedValidationRuleSetVersionInfo> result = new ArrayList<>();
        suspectReasonRuleSetVersionMap.entrySet().stream().forEach(version -> {
            result.add(new DetailedValidationRuleSetVersionInfo(version.getKey(),version.getValue(), suspectReasonMap));
        });
        return result;
    }

}
