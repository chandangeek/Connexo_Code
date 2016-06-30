package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.cbo.QualityCodeCategory;
import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.readings.ReadingQuality;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationRuleSetVersion;
import com.elster.jupiter.validation.rest.PropertyUtils;
import com.elster.jupiter.validation.rest.ValidationRuleInfo;
import com.elster.jupiter.validation.rest.ValidationRuleInfoFactory;
import com.elster.jupiter.validation.rest.ValidationRuleSetInfo;
import com.elster.jupiter.validation.rest.ValidationRuleSetVersionInfo;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.DeviceValidation;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.NumericalRegister;

import com.google.common.collect.ImmutableList;

import javax.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.streams.DecoratedStream.decorate;

/**
 * Copyrights EnergyICT
 * Date: 28/05/15
 * Time: 10:44
 */
public class ValidationInfoFactory {

    private static final List<QualityCodeCategory> QUALITY_CODE_CATEGORIES = Arrays.asList(
            QualityCodeCategory.DIAGNOSTICS,
            QualityCodeCategory.POWERQUALITY,
            QualityCodeCategory.TAMPER,
            QualityCodeCategory.DATACOLLECTION
    );

    private final ValidationRuleInfoFactory validationRuleInfoFactory;
    private final EstimationRuleInfoFactory estimationRuleInfoFactory;
    private final PropertyUtils propertyUtils;
    private final Thesaurus thesaurus;
    private final ResourceHelper resourceHelper;

    @Inject
    public ValidationInfoFactory(ValidationRuleInfoFactory validationRuleInfoFactory, EstimationRuleInfoFactory estimationRuleInfoFactory, PropertyUtils propertyUtils, Thesaurus thesaurus, ResourceHelper resourceHelper) {
        this.validationRuleInfoFactory = validationRuleInfoFactory;
        this.estimationRuleInfoFactory = estimationRuleInfoFactory;
        this.propertyUtils = propertyUtils;
        this.thesaurus = thesaurus;
        this.resourceHelper = resourceHelper;
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
                .forEach(reg -> {
                    monitorValidationInfo.detailedValidationRegister.add(new DetailedValidationRegisterInfo(reg.getKey(), new Long(reg
                            .getValue()
                            .size())));
                });
        return monitorValidationInfo;
    }

    MonitorValidationInfo createMonitorValidationInfoForValidationStatues(List<DataValidationStatus> dataValidationStatuses, ValidationStatusInfo validationStatus) {
        MonitorValidationInfo monitorValidationInfo = new MonitorValidationInfo();
        monitorValidationInfo.total = 0L;
        monitorValidationInfo.validationStatus = validationStatus;
        monitorValidationInfo.detailedRuleSets = getSuspectReasonMapForValidationStatuses(dataValidationStatuses);
        monitorValidationInfo.total += dataValidationStatuses.size();

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

    VeeReadingInfo createVeeReadingInfo(Channel channel, DataValidationStatus dataValidationStatus, DeviceValidation deviceValidation) {
        VeeReadingInfo veeReadingInfo = new VeeReadingInfo();
        veeReadingInfo.dataValidated = dataValidationStatus.completelyValidated();
        veeReadingInfo.mainValidationInfo.validationResult = ValidationStatus.forResult(deviceValidation.getValidationResult(dataValidationStatus.getReadingQualities()));
        veeReadingInfo.mainValidationInfo.validationRules = validationRuleInfoFactory.createInfosForDataValidationStatus(dataValidationStatus);
        veeReadingInfo.mainValidationInfo.estimatedByRule = estimationRuleInfoFactory.createEstimationRuleInfo(dataValidationStatus.getReadingQualities());
        if (channel.getCalculatedReadingType(dataValidationStatus.getReadingTimestamp()).isPresent()) {
            veeReadingInfo.bulkValidationInfo.validationResult = ValidationStatus.forResult(deviceValidation.getValidationResult(dataValidationStatus.getBulkReadingQualities()));
            veeReadingInfo.bulkValidationInfo.validationRules = validationRuleInfoFactory.createInfosForBulkDataValidationStatus(dataValidationStatus);
            veeReadingInfo.bulkValidationInfo.estimatedByRule = estimationRuleInfoFactory.createEstimationRuleInfo(dataValidationStatus.getBulkReadingQualities());
        }
        return veeReadingInfo;
    }

    VeeReadingInfo createVeeReadingInfoWithModificationFlags(Channel channel, DataValidationStatus dataValidationStatus, DeviceValidation deviceValidation, IntervalReadingRecord reading, Boolean validationActive) {
        VeeReadingInfo veeReadingInfo = createVeeReadingInfo(channel, dataValidationStatus, deviceValidation);
        veeReadingInfo.readingQualities = getReadingQualities(reading);
        veeReadingInfo.validationActive = validationActive;
        setVeeReadingValueInfo(veeReadingInfo.mainValidationInfo, reading, dataValidationStatus.getReadingQualities());
        if (channel.getReadingType().getCalculatedReadingType().isPresent()) {
            setVeeReadingValueInfo(veeReadingInfo.bulkValidationInfo, reading, dataValidationStatus.getBulkReadingQualities());
        }
        return veeReadingInfo;
    }

    private void setVeeReadingValueInfo(VeeReadingValueInfo info, IntervalReadingRecord reading, Collection<? extends ReadingQuality> readingQualities) {
        info.valueModificationFlag = ReadingModificationFlag.getModificationFlag(reading, readingQualities);
        List<? extends ReadingQuality> confirmedQualities = getConfirmedQualities(reading, readingQualities);
        info.isConfirmed = !confirmedQualities.isEmpty();
        info.confirmedInApps = confirmedQualities.stream()
                .map(ReadingQuality::getType)
                .map(ReadingQualityType::system)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(resourceHelper::getApplicationInfo)
                .collect(Collectors.collectingAndThen(Collectors.toSet(), s -> s.isEmpty() ? null : s));
    }

    private List<ReadingQualityInfo> getReadingQualities(IntervalReadingRecord intervalReadingRecord) {
        if (intervalReadingRecord == null) {
            return Collections.emptyList();
        }
        return intervalReadingRecord.getReadingQualities().stream()
                .filter(ReadingQualityRecord::isActual)
                .map(ReadingQuality::getType)
                .distinct()
                // TODO: the place for refactoring of CXO-1437/CXO-1438
                .filter(type -> type.system().isPresent())
                .filter(type -> type.qualityIndex().isPresent())
                .filter(type -> QUALITY_CODE_CATEGORIES.contains(type.category().get()))
                .map(this::createReadingQualityInfo)
                .collect(Collectors.toList());
    }


    MinimalVeeReadingValueInfo createMainVeeReadingInfo(DataValidationStatus dataValidationStatus, DeviceValidation deviceValidation, IntervalReadingRecord reading) {
        MinimalVeeReadingValueInfo veeReadingInfo = new MinimalVeeReadingValueInfo();
        veeReadingInfo.validationResult = ValidationStatus.forResult(deviceValidation.getValidationResult(dataValidationStatus.getReadingQualities()));
        veeReadingInfo.valueModificationFlag = ReadingModificationFlag.getModificationFlag(reading, dataValidationStatus.getReadingQualities());
        veeReadingInfo.isConfirmed = isConfirmedData(reading, dataValidationStatus.getReadingQualities());
        veeReadingInfo.action = decorate(dataValidationStatus.getReadingQualities()
                .stream())
                .filter(quality -> quality.getType().hasValidationCategory() || quality.getType().isSuspect())
                .map(readingQuality -> getReadingQualityValidationAction(readingQuality, veeReadingInfo))
                .sorted(Comparator.<ValidationAction>reverseOrder())
                .findFirst()
                .orElse(null);
        veeReadingInfo.estimatedByRule = dataValidationStatus.getReadingQualities().stream()
                .map(ReadingQuality::getType)
                .anyMatch(ReadingQualityType::hasEstimatedCategory);
        return veeReadingInfo;
    }

    MinimalVeeReadingValueInfo createBulkVeeReadingInfo(Channel channel, DataValidationStatus dataValidationStatus, DeviceValidation deviceValidation, IntervalReadingRecord reading) {
        if (channel.getCalculatedReadingType(dataValidationStatus.getReadingTimestamp()).isPresent()) {
            MinimalVeeReadingValueInfo veeReadingInfo = new MinimalVeeReadingValueInfo();
            veeReadingInfo.validationResult = ValidationStatus.forResult(deviceValidation.getValidationResult(dataValidationStatus.getBulkReadingQualities()));
            veeReadingInfo.valueModificationFlag = ReadingModificationFlag.getModificationFlag(reading, dataValidationStatus.getBulkReadingQualities());
            veeReadingInfo.isConfirmed = isConfirmedData(reading, dataValidationStatus.getBulkReadingQualities());
            veeReadingInfo.action = decorate(dataValidationStatus.getBulkReadingQualities()
                    .stream())
                    .filter(quality -> quality.getType().hasValidationCategory() || quality.getType().isSuspect())
                    .map(readingQuality -> getReadingQualityValidationAction(readingQuality, veeReadingInfo))
                    .sorted(Comparator.<ValidationAction>reverseOrder())
                    .findFirst()
                    .orElse(null);
            veeReadingInfo.estimatedByRule = dataValidationStatus.getBulkReadingQualities().stream()
                    .map(ReadingQuality::getType)
                    .anyMatch(ReadingQualityType::hasEstimatedCategory);
            return veeReadingInfo;
        }
        return null;
    }

    private ValidationAction getReadingQualityValidationAction(ReadingQuality readingQuality, MinimalVeeReadingValueInfo veeReadingValueInfo) {
        if (readingQuality.getType().system().isPresent()) {
            if (readingQuality.getType().system().get() == QualityCodeSystem.MDM) {
                veeReadingValueInfo.validationResult = ValidationStatus.OK;
                return ValidationAction.WARN_ONLY;
            }
        }
        return readingQuality.getType().isSuspect() ? ValidationAction.FAIL : ValidationAction.WARN_ONLY;
    }

    boolean isConfirmedData(BaseReadingRecord reading, Collection<? extends ReadingQuality> qualities) {
        return !getConfirmedQualities(reading, qualities).isEmpty();
    }

    List<? extends ReadingQuality> getConfirmedQualities(BaseReadingRecord reading, Collection<? extends ReadingQuality> qualities) {
        if (reading != null && reading.confirmed()) {
            return qualities.stream().filter(this::confirmedReadingQuality).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private boolean confirmedReadingQuality(ReadingQuality readingQuality) {
        return readingQuality.getType().qualityIndex().orElse(null) == QualityCodeIndex.ACCEPTED;
    }

    DetailedValidationInfo createMinimalValidationInfo(Boolean active) {
        DetailedValidationInfo detailedValidationInfo = new DetailedValidationInfo();
        detailedValidationInfo.validationActive = active;
        return detailedValidationInfo;
    }

    DetailedValidationInfo createDetailedValidationInfo(Boolean active, List<DataValidationStatus> dataValidationStatuses, Optional<Instant> lastChecked) {
        DetailedValidationInfo detailedValidationInfo = createMinimalValidationInfo(active);
        detailedValidationInfo.dataValidated = isDataCompletelyValidated(dataValidationStatuses);
        detailedValidationInfo.suspectReason = getSuspectReasonMap(dataValidationStatuses)
                .entrySet()
                .stream()
                .map(validationRuleInfoLongEntry -> new ValidationRuleInfoWithNumber(validationRuleInfoLongEntry.getKey(), validationRuleInfoLongEntry
                        .getValue()))
                .collect(Collectors.toSet());
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

    MinimalVeeReadingInfo createMinimalVeeReadingInfo(Channel channel, DataValidationStatus dataValidationStatus, DeviceValidation deviceValidation) {
        MinimalVeeReadingInfo veeReadingInfo = new MinimalVeeReadingInfo();
        veeReadingInfo.dataValidated = dataValidationStatus.completelyValidated();
        veeReadingInfo.mainValidationInfo = createMainVeeReadingInfo(dataValidationStatus, deviceValidation);
        veeReadingInfo.bulkValidationInfo = createBulkVeeReadingInfo(channel, dataValidationStatus, deviceValidation);
        return veeReadingInfo;
    }

    MinimalVeeReadingValueInfo createMainVeeReadingInfo(DataValidationStatus dataValidationStatus, DeviceValidation deviceValidation) {
        MinimalVeeReadingValueInfo veeReadingInfo = new MinimalVeeReadingValueInfo();
        veeReadingInfo.validationResult = ValidationStatus.forResult(deviceValidation.getValidationResult(dataValidationStatus.getReadingQualities()));
        veeReadingInfo.action = decorate(dataValidationStatus.getReadingQualities()
                .stream())
                .filter(quality -> quality.getType().hasValidationCategory() || quality.getType().isSuspect())
                .map(quality -> quality.getType().isSuspect() ? ValidationAction.FAIL : ValidationAction.WARN_ONLY)
                .sorted(Comparator.<ValidationAction>reverseOrder())
                .findFirst()
                .orElse(null);
        veeReadingInfo.estimatedByRule = dataValidationStatus.getReadingQualities().stream()
                .map(ReadingQuality::getType)
                .anyMatch(ReadingQualityType::hasEstimatedCategory);
        return veeReadingInfo;
    }

    MinimalVeeReadingValueInfo createBulkVeeReadingInfo(Channel channel, DataValidationStatus dataValidationStatus, DeviceValidation deviceValidation) {
        if (channel.getCalculatedReadingType(dataValidationStatus.getReadingTimestamp()).isPresent()) {
            MinimalVeeReadingValueInfo veeReadingInfo = new MinimalVeeReadingValueInfo();
            veeReadingInfo.validationResult = ValidationStatus.forResult(deviceValidation.getValidationResult(dataValidationStatus.getBulkReadingQualities()));
            veeReadingInfo.action = decorate(dataValidationStatus.getBulkReadingQualities()
                    .stream())
                    .filter(quality -> quality.getType().hasValidationCategory() || quality.getType().isSuspect())
                    .map(quality -> quality.getType().isSuspect() ? ValidationAction.FAIL : ValidationAction.WARN_ONLY)
                    .sorted(Comparator.<ValidationAction>reverseOrder())
                    .findFirst()
                    .orElse(null);
            veeReadingInfo.estimatedByRule = dataValidationStatus.getBulkReadingQualities().stream()
                    .map(ReadingQuality::getType)
                    .anyMatch(ReadingQualityType::hasEstimatedCategory);
            return veeReadingInfo;
        }
        return null;
    }

    ReadingQualityInfo createReadingQualityInfo(ReadingQualityType type) {
        ReadingQualityInfo info = new ReadingQualityInfo();
        info.id = type.getCode();
        info.name = thesaurus.getString(type.qualityIndex().get().getTranslationKey().getKey(), type.qualityIndex().get().getTranslationKey().getDefaultFormat());
        info.application = resourceHelper.getApplicationInfo(type.system().get());
        return info;
    }
}
