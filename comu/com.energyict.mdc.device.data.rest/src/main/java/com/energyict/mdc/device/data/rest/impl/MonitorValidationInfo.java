package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationRuleSetVersion;
import com.elster.jupiter.validation.rest.ValidationRuleInfo;

import java.time.Instant;
import java.util.*;

/**
 * Created by tgr on 5/09/2014.
 */
public class MonitorValidationInfo {

    public Boolean validationActive;
    public Boolean dataValidated;
    public List<DetailedValidationRuleSetInfo> detailedRuleSets;
    public Long lastChecked;
    public Long total;

    public MonitorValidationInfo(Boolean active, List<DataValidationStatus> dataValidationStatuses, Optional<Instant> lastChecked) {
        validationActive = active;
        total = 0L;
        this.dataValidated = isDataCompletelyValidated(dataValidationStatuses);
        this.detailedRuleSets = getSuspectReasonMap(dataValidationStatuses);
        if (lastChecked.isPresent()) {
            this.lastChecked = lastChecked.get().toEpochMilli();
        }
        else {
            this.lastChecked = null;
        }
    }

    public MonitorValidationInfo() {

    }

    private boolean isDataCompletelyValidated(List<DataValidationStatus> dataValidationStatuses) {
        return dataValidationStatuses.stream().allMatch(DataValidationStatus::completelyValidated);
    }

    private List<DetailedValidationRuleSetInfo> getSuspectReasonMap(List<DataValidationStatus> dataValidationStatuses) {
        Map<ValidationRule, Long> suspectReasonMap = new HashMap<>();
        Map<ValidationRuleSet, Long> suspectReasonRuleSetMap = new HashMap<>();
        Map<ValidationRuleSetVersion, Long> suspectReasonRuleSetVersionMap = new HashMap<>();
        dataValidationStatuses.stream().forEach(s -> fillSuspectReasonMap(s.getOffendedRules(), suspectReasonMap, suspectReasonRuleSetVersionMap, suspectReasonRuleSetMap));
        return computeRuleSetVersionChain(suspectReasonRuleSetMap, suspectReasonRuleSetVersionMap, suspectReasonMap);
    }

    private List<DetailedValidationRuleSetInfo> computeRuleSetVersionChain(Map<ValidationRuleSet, Long> suspectReasonRuleSetMap, Map<ValidationRuleSetVersion, Long> suspectReasonRuleSetVersionMap, Map<ValidationRule, Long> suspectReasonMap) {
        List<DetailedValidationRuleSetInfo> result = new ArrayList<>();
        suspectReasonRuleSetMap.entrySet().stream().forEach(ruleSet -> {
            result.add(new DetailedValidationRuleSetInfo(ruleSet.getKey(),ruleSet.getValue(),suspectReasonRuleSetVersionMap, suspectReasonMap));
            this.total += ruleSet.getValue();
        });
        return result;
    }

    private void fillSuspectReasonMap(Collection<ValidationRule> validationRules, Map<ValidationRule, Long> suspectReasonMap, Map<ValidationRuleSetVersion, Long> suspectReasonRuleSetVersionMap, Map<ValidationRuleSet, Long> suspectReasonRuleSetMap) {
        validationRules.forEach(r -> {

            suspectReasonRuleSetMap.putIfAbsent(r.getRuleSetVersion().getRuleSet(), 0L);
            suspectReasonRuleSetMap.compute(r.getRuleSetVersion().getRuleSet(), (k, v) -> v + 1);

            suspectReasonRuleSetVersionMap.putIfAbsent(r.getRuleSetVersion(), 0L);
            suspectReasonRuleSetVersionMap.compute(r.getRuleSetVersion(), (k, v) -> v + 1);

            suspectReasonMap.putIfAbsent(r, 0L);
            suspectReasonMap.compute(r, (k, v) -> v + 1);
        });
    }
}
