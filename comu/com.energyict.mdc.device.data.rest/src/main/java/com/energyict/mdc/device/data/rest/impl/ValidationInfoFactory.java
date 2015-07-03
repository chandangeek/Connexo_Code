package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationRuleSetVersion;
import com.elster.jupiter.validation.rest.*;
import com.energyict.mdc.device.data.*;
import com.energyict.mdc.device.data.rest.ValueModificationFlag;
import com.google.common.collect.ImmutableList;

import javax.inject.Inject;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Copyrights EnergyICT
 * Date: 28/05/15
 * Time: 10:44
 */
public class ValidationInfoFactory {

    private final ValidationRuleInfoFactory validationRuleInfoFactory;
    private final PropertyUtils propertyUtils;

    @Inject
    public ValidationInfoFactory(ValidationRuleInfoFactory validationRuleInfoFactory, PropertyUtils propertyUtils) {
        this.validationRuleInfoFactory = validationRuleInfoFactory;
        this.propertyUtils = propertyUtils;
    }

    DetailedValidationRuleInfo createDetailedValidationRuleInfo(ValidationRule validationRule, Long total) {
        DetailedValidationRuleInfo validationRuleInfo = new DetailedValidationRuleInfo();
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
        validationRuleInfo.total = total;
        return validationRuleInfo;
    }

    DetailedValidationRuleSetVersionInfo createDetailedValidationRuleSetVersionInfo(ValidationRuleSetVersion validationRuleSetVersion, Long total, Map<ValidationRule, Long> suspectReasonMap) {
        DetailedValidationRuleSetVersionInfo detailedValidationRuleSetVersionInfo = new DetailedValidationRuleSetVersionInfo();

        detailedValidationRuleSetVersionInfo.id = validationRuleSetVersion.getId();
        detailedValidationRuleSetVersionInfo.status = validationRuleSetVersion.getStatus();
        detailedValidationRuleSetVersionInfo.description = validationRuleSetVersion.getDescription();
        Optional.ofNullable(validationRuleSetVersion.getStartDate()).ifPresent(sd -> {
            detailedValidationRuleSetVersionInfo.startDate = sd.toEpochMilli();
        });
        Optional.ofNullable(validationRuleSetVersion.getEndDate()).ifPresent(ed -> {
            detailedValidationRuleSetVersionInfo.endDate = ed.toEpochMilli();
        });
        detailedValidationRuleSetVersionInfo.ruleSet = new ValidationRuleSetInfo(validationRuleSetVersion.getRuleSet());
        List<? extends ValidationRule> validationRules = validationRuleSetVersion.getRules();
        detailedValidationRuleSetVersionInfo.numberOfRules = validationRules.size();
        detailedValidationRuleSetVersionInfo.numberOfInactiveRules = (int) validationRules.stream().filter(r -> !r.isActive()).count();
        detailedValidationRuleSetVersionInfo.total = total;
        Map<ValidationRule, Long> rules = suspectReasonMap.entrySet().stream()
                .filter(rule -> rule.getKey().getRuleSetVersion().getId() == validationRuleSetVersion.getId())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        detailedValidationRuleSetVersionInfo.detailedRules = computeRules(rules);
        return detailedValidationRuleSetVersionInfo;
    }

    DetailedValidationRuleSetInfo createDetailedValidationRuleSetInfo(ValidationRuleSet ruleSet, Long total, Map<ValidationRuleSetVersion, Long> suspectReasonRuleSetVersionMap, Map<ValidationRule, Long> suspectReasonMap) {
        DetailedValidationRuleSetInfo detailedValidationRuleSetInfo = new DetailedValidationRuleSetInfo();
        detailedValidationRuleSetInfo.id = ruleSet.getId();
        detailedValidationRuleSetInfo.name = ruleSet.getName();
        detailedValidationRuleSetInfo.total = total;
        Map<ValidationRuleSetVersion, Long> versions = suspectReasonRuleSetVersionMap.entrySet().stream()
                .filter(version -> version.getKey().getRuleSet().getId() == ruleSet.getId())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        detailedValidationRuleSetInfo.detailedRuleSetVersions = computeRuleSetVersionChain(versions, suspectReasonMap);
        return detailedValidationRuleSetInfo;
    }

    MonitorValidationInfo createMonitorValidationInfoForLoadProfileAndRegister(Map<LoadProfile, List<DataValidationStatus>> loadProfileStatus, Map<NumericalRegister, List<DataValidationStatus>> registerStatus, ValidationStatusInfo validationStatus) {
        MonitorValidationInfo monitorValidationInfo = new MonitorValidationInfo();
        monitorValidationInfo.total = loadProfileStatus.entrySet().stream().flatMap(m -> m.getValue().stream()).collect(Collectors.counting()) +
                registerStatus.entrySet().stream().flatMap(m -> m.getValue().stream()).collect(Collectors.counting());

        List<DataValidationStatus> dataValidationStatuses = loadProfileStatus.entrySet().stream()
                .flatMap(m -> m.getValue().stream()).collect(Collectors.toList());
        dataValidationStatuses.addAll(registerStatus.entrySet().stream()
                .flatMap(m -> m.getValue().stream()).collect(Collectors.toList()));

        monitorValidationInfo.validationStatus = validationStatus;
        monitorValidationInfo.detailedValidationLoadProfile = new ArrayList<>();
        monitorValidationInfo.detailedValidationRegister = new ArrayList<>();
        loadProfileStatus.entrySet().stream()
                .sorted((lps1, lps2) -> lps1.getKey().getLoadProfileSpec().getLoadProfileType().getName().compareTo(lps2.getKey().getLoadProfileSpec().getLoadProfileType().getName()))
                .forEach(lp -> {
                    monitorValidationInfo.detailedValidationLoadProfile.add(new DetailedValidationLoadProfileInfo(lp.getKey(), new Long(lp.getValue().size())));
                });
        registerStatus.entrySet().stream()
                .sorted((regs1, regs2) -> regs1.getKey().getRegisterSpec().getReadingType().getFullAliasName().compareTo(regs2.getKey().getRegisterSpec().getReadingType().getFullAliasName()))
                .forEach( reg -> {
                    monitorValidationInfo.detailedValidationRegister.add(new DetailedValidationRegisterInfo(reg.getKey(),new Long(reg.getValue().size())));
                });
        return monitorValidationInfo;
    }

    MonitorValidationInfo createMonitorValidationInfoForValidationStatues(List<DataValidationStatus> dataValidationStatuses, ValidationStatusInfo validationStatus) {
        MonitorValidationInfo monitorValidationInfo = new MonitorValidationInfo();
        monitorValidationInfo.total = 0L;
        monitorValidationInfo.validationStatus = validationStatus;
        monitorValidationInfo.detailedRuleSets = getSuspectReasonMapForValidationStatuses(dataValidationStatuses);
        monitorValidationInfo.total += monitorValidationInfo.detailedRuleSets.size();
        return monitorValidationInfo;
    }

    private List<DetailedValidationRuleSetInfo> getSuspectReasonMapForValidationStatuses(List<DataValidationStatus> dataValidationStatuses) {
        Map<ValidationRule, Long> suspectReasonMap = new HashMap<>();
        Map<ValidationRuleSet, Long> suspectReasonRuleSetMap = new HashMap<>();
        Map<ValidationRuleSetVersion, Long> suspectReasonRuleSetVersionMap = new HashMap<>();
        dataValidationStatuses.stream().forEach(s -> {
            ImmutableList.Builder<ValidationRule> validationRules = ImmutableList.builder();
            validationRules.addAll(s.getOffendedRules());
            validationRules.addAll(s.getBulkOffendedRules());
            fillSuspectReasonMap(validationRules.build(), suspectReasonMap, suspectReasonRuleSetVersionMap, suspectReasonRuleSetMap);
        });
        return computeRuleSetVersionChain(suspectReasonRuleSetMap, suspectReasonRuleSetVersionMap, suspectReasonMap);
    }

    private List<DetailedValidationRuleSetInfo> computeRuleSetVersionChain(Map<ValidationRuleSet, Long> suspectReasonRuleSetMap, Map<ValidationRuleSetVersion, Long> suspectReasonRuleSetVersionMap, Map<ValidationRule, Long> suspectReasonMap) {
        List<DetailedValidationRuleSetInfo> result = new ArrayList<>();
        suspectReasonRuleSetMap.entrySet().stream().forEach(ruleSet -> {
            result.add(createDetailedValidationRuleSetInfo(ruleSet.getKey(), ruleSet.getValue(), suspectReasonRuleSetVersionMap, suspectReasonMap));
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

    private List<DetailedValidationRuleSetVersionInfo> computeRuleSetVersionChain(Map<ValidationRuleSetVersion, Long> suspectReasonRuleSetVersionMap, Map<ValidationRule, Long> suspectReasonMap) {
        List<DetailedValidationRuleSetVersionInfo> result = new ArrayList<>();
        suspectReasonRuleSetVersionMap.entrySet().stream().forEach(version -> {
            result.add(createDetailedValidationRuleSetVersionInfo(version.getKey(), version.getValue(), suspectReasonMap));
        });
        return result;
    }

    private List<DetailedValidationRuleInfo> computeRules(Map<ValidationRule, Long> suspectReasonMap) {
        List<DetailedValidationRuleInfo> result = new ArrayList<>();
        suspectReasonMap.entrySet().stream().forEach(rule -> {
            result.add(createDetailedValidationRuleInfo(rule.getKey(), rule.getValue()));
        });
        return result;
    }

    ValidationInfo createValidationInfoFor(Map.Entry<Channel, DataValidationStatus> entry, DeviceValidation deviceValidation, ReadingModificationFlag readingModificationFlag) {
        ValidationInfo validationInfo = new ValidationInfo();
        DataValidationStatus dataValidationStatus = entry.getValue();
        validationInfo.dataValidated = dataValidationStatus.completelyValidated();
        validationInfo.mainValidationInfo.validationRules = validationRuleInfoFactory.createInfosForDataValidationStatus(dataValidationStatus);
        validationInfo.mainValidationInfo.validationResult = ValidationStatus.forResult(deviceValidation.getValidationResult(dataValidationStatus.getReadingQualities()));
        validationInfo.mainValidationInfo.valueModificationFlag = ValueModificationFlag.getModificationFlag(dataValidationStatus.getReadingQualities(), readingModificationFlag);
        if (entry.getKey().getReadingType().isCumulative() && entry.getKey().getReadingType().getCalculatedReadingType().isPresent()) {
            validationInfo.bulkValidationInfo.validationRules = validationRuleInfoFactory.createInfosForBulkDataValidationStatus(dataValidationStatus);
            validationInfo.bulkValidationInfo.validationResult = ValidationStatus.forResult(deviceValidation.getValidationResult(dataValidationStatus.getBulkReadingQualities()));
            validationInfo.bulkValidationInfo.valueModificationFlag = ValueModificationFlag.getModificationFlag(dataValidationStatus.getBulkReadingQualities(), readingModificationFlag);
        }
        return validationInfo;
    }




    DetailedValidationInfo createDetailedValidationInfo(Boolean active, List<DataValidationStatus> dataValidationStatuses, Optional<Instant> lastChecked) {
        DetailedValidationInfo detailedValidationInfo = new DetailedValidationInfo();
        detailedValidationInfo.validationActive = active;
        detailedValidationInfo.dataValidated = isDataCompletelyValidated(dataValidationStatuses);
        detailedValidationInfo.suspectReason = getSuspectReasonMap(dataValidationStatuses).entrySet();
        if (lastChecked.isPresent()) {
            detailedValidationInfo.lastChecked = lastChecked.get().toEpochMilli();
        } else {
            detailedValidationInfo.lastChecked = null;
        }
        return detailedValidationInfo;
    }

    private boolean isDataCompletelyValidated(List<DataValidationStatus> dataValidationStatuses) {
        return dataValidationStatuses.stream().allMatch(DataValidationStatus::completelyValidated);
    }

    private Map<ValidationRuleInfo, Long> getSuspectReasonMap(List<DataValidationStatus> dataValidationStatuses) {
        Map<ValidationRule, Long> suspectReasonMap = new HashMap<>();
        dataValidationStatuses.stream().forEach(s -> {
            ImmutableList.Builder<ValidationRule> validationRules = ImmutableList.builder();
            validationRules.addAll(s.getOffendedRules());
            validationRules.addAll(s.getBulkOffendedRules());
            fillSuspectReasonMap(validationRules.build(), suspectReasonMap);

        });
        return validationRuleInfoFactory.createInfosForSuspectReasons(suspectReasonMap);
    }

    private void fillSuspectReasonMap(Collection<ValidationRule> validationRules, Map<ValidationRule, Long> suspectReasonMap) {
        validationRules.forEach(r -> {
            suspectReasonMap.putIfAbsent(r, 0L);
            suspectReasonMap.compute(r, (k, v) -> v + 1);
        });
    }
}
