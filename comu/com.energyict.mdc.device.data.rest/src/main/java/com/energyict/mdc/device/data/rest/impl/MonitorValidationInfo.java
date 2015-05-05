package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationRuleSetVersion;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.NumericalRegister;

import java.util.*;
import java.util.stream.Collectors;

public class MonitorValidationInfo {


    public ValidationStatusInfo validationStatus;
    public Long total;
    public List<DetailedValidationRuleSetInfo> detailedRuleSets;
    public List<DetailedValidationLoadProfileInfo> detailedValidationLoadProfile;
    public List<DetailedValidationRegisterInfo> detailedValidationRegister;

    public MonitorValidationInfo(List<DataValidationStatus> dataValidationStatuses, ValidationStatusInfo validationStatus) {
        total = 0L;
        this.validationStatus = validationStatus;
        this.detailedRuleSets = getSuspectReasonMap(dataValidationStatuses);
    }

    public MonitorValidationInfo(Map<LoadProfile, List<DataValidationStatus>> loadProfileStatus , Map<NumericalRegister, List<DataValidationStatus>> registerStatus, ValidationStatusInfo validationStatus) {
        total = loadProfileStatus.entrySet().stream().flatMap(m -> m.getValue().stream()).collect(Collectors.counting()) +
                registerStatus.entrySet().stream().flatMap(m -> m.getValue().stream()).collect(Collectors.counting());
        List<DataValidationStatus> dataValidationStatuses = loadProfileStatus.entrySet().stream().flatMap(m -> m.getValue().stream()).collect(Collectors.toList());
        dataValidationStatuses.addAll(registerStatus.entrySet().stream().flatMap(m -> m.getValue().stream()).collect(Collectors.toList()));
        this.validationStatus = validationStatus;
        this.detailedValidationLoadProfile = new ArrayList<>();
        this.detailedValidationRegister = new ArrayList<>();
        loadProfileStatus.entrySet().stream().forEach( lp -> {
            this.detailedValidationLoadProfile.add(new DetailedValidationLoadProfileInfo(lp.getKey(),new Long(lp.getValue().size())));
        });
        registerStatus.entrySet().stream().forEach( lp -> {
            this.detailedValidationRegister.add(new DetailedValidationRegisterInfo(lp.getKey(),new Long(lp.getValue().size())));
        });
    }

    public MonitorValidationInfo() {
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
