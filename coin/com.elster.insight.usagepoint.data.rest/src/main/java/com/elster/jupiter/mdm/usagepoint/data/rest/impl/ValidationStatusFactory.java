/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.estimation.EstimationRule;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.mdm.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.mdm.usagepoint.data.UsagePointDataCompletionService;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.readings.ReadingQuality;
import com.elster.jupiter.util.Ranges;
import com.elster.jupiter.util.streams.Functions;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.rest.ValidationRuleInfo;
import com.elster.jupiter.validation.rest.ValidationRuleSetInfo;
import com.elster.jupiter.validation.rest.ValidationRuleSetVersionInfo;

import com.google.common.collect.Range;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ValidationStatusFactory {
    private final Clock clock;
    private final ValidationService validationService;
    private final EstimationService estimationService;
    private final UsagePointConfigurationService usagePointConfigurationService;
    private final UsagePointDataCompletionService usagePointDataCompletionService;

    @Inject
    public ValidationStatusFactory(Clock clock, ValidationService validationService, EstimationService estimationService, UsagePointConfigurationService usagePointConfigurationService, UsagePointDataCompletionService usagePointDataCompletionService) {
        this.clock = clock;
        this.validationService = validationService;
        this.estimationService = estimationService;
        this.usagePointConfigurationService = usagePointConfigurationService;
        this.usagePointDataCompletionService = usagePointDataCompletionService;
    }

    public UsagePointValidationStatusInfo getValidationStatusInfo(EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration, MetrologyContract metrologyContract,
                                                                  Channel channel, Range<Instant> interval) {
        UsagePointValidationStatusInfo info = new UsagePointValidationStatusInfo();
        ValidationEvaluator validationEvaluator = validationService.getEvaluator();
        info.validationActive = validationEvaluator.isValidationEnabled(channel);
        info.lastChecked = usagePointDataCompletionService.getLastChecked(effectiveMetrologyConfiguration.getUsagePoint(), metrologyContract.getMetrologyPurpose(), channel.getMainReadingType()).orElse(null);
        if (interval != null) {
            setReasonInfo(validationEvaluator.getValidationStatus(EnumSet.of(QualityCodeSystem.MDM), channel, Collections.emptyList(), interval != null ? interval : lastMonth()), info);
        }
        info.allDataValidated = validationEvaluator.isAllDataValidated(Collections.singletonList(channel));
        info.hasSuspects = validationEvaluator.areSuspectsPresent(EnumSet.of(QualityCodeSystem.MDM), channel, interval != null ? interval : lastMonth());
        return info;
    }

    public Map<ReadingTypeDeliverable, UsagePointValidationStatusInfo> getValidationStatusInfoForDeliverables(EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration,
                                                                                                              MetrologyContract metrologyContract, Range<Instant> interval) {
        ChannelsContainer container = effectiveMetrologyConfiguration.getChannelsContainer(metrologyContract).get();
        ValidationEvaluator validationEvaluator = validationService.getEvaluator(container);
        Map<ReadingTypeDeliverable, UsagePointValidationStatusInfo> result = new HashMap<>();

        for (ReadingTypeDeliverable readingTypeDeliverable : metrologyContract.getDeliverables()) {
            UsagePointValidationStatusInfo info = new UsagePointValidationStatusInfo();
            container.getChannel(readingTypeDeliverable.getReadingType()).ifPresent(channel -> {
                info.validationActive = validationEvaluator.isValidationEnabled(channel);
                info.lastChecked = usagePointDataCompletionService.getLastChecked(effectiveMetrologyConfiguration.getUsagePoint(), metrologyContract.getMetrologyPurpose(), readingTypeDeliverable.getReadingType()).orElse(null);
                info.allDataValidated = validationEvaluator.isAllDataValidated(Collections.singletonList(channel));
                info.hasSuspects = validationEvaluator.areSuspectsPresent(EnumSet.of(QualityCodeSystem.MDM), channel, interval != null ? interval : lastMonth());
                if (interval != null) {
                    setReasonInfo(getValidationStatus(channel.getMainReadingType(), interval, validationEvaluator, effectiveMetrologyConfiguration, metrologyContract), info);
                }
            });
            result.put(readingTypeDeliverable, info);
        }
        return result;
    }

    private List<DataValidationStatus> getValidationStatus(ReadingType readingType, Range<Instant> interval, ValidationEvaluator validationEvaluator,
                                                   EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration, MetrologyContract metrologyContract) {
        UsagePoint usagePoint = effectiveMetrologyConfiguration.getUsagePoint();

        return usagePoint.getEffectiveMetrologyConfigurations().stream()
                .map(emc -> getMetrologyContract(emc.getMetrologyConfiguration(), metrologyContract.getMetrologyPurpose()).flatMap(emc::getChannelsContainer))
                .flatMap(Functions.asStream())
                .filter(channelsContainer -> Ranges.nonEmptyIntersection(interval, channelsContainer.getInterval().toOpenClosedRange()).isPresent())
                .map(channelsContainer -> channelsContainer.getChannel(readingType))
                .flatMap(Functions.asStream())
                .flatMap(channel -> validationEvaluator.getValidationStatus(EnumSet.of(QualityCodeSystem.MDM), channel, Collections.emptyList(),
                        Ranges.nonEmptyIntersection(interval, channel.getChannelsContainer().getInterval().toOpenClosedRange()).get()).stream())
                .collect(Collectors.toList());
    }

    private Optional<MetrologyContract> getMetrologyContract(MetrologyConfiguration metrologyConfiguration, MetrologyPurpose purpose){
        return metrologyConfiguration.getContracts().stream().filter(metrologyContract -> metrologyContract.getMetrologyPurpose().equals(purpose)).findFirst();
    }

    public UsagePointValidationStatusInfo getValidationStatusInfo(EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration, MetrologyContract metrologyContract, ChannelsContainer channelsContainer) {
        UsagePointValidationStatusInfo info = new UsagePointValidationStatusInfo();
        // force update for validation statuses
        // used here, since further methods (to get validation status) do not include status update
        validationService.forceUpdateValidationStatus(channelsContainer);
        ValidationEvaluator validationEvaluator = validationService.getEvaluator();
        info.validationActive = validationService.isValidationActive(channelsContainer);
        info.lastChecked = usagePointDataCompletionService.getLastChecked(effectiveMetrologyConfiguration.getUsagePoint(), metrologyContract.getMetrologyPurpose()).orElse(null);
        info.allDataValidated = validationEvaluator.isAllDataValidated(channelsContainer);
        info.hasSuspects = validationEvaluator.areSuspectsPresent(EnumSet.of(QualityCodeSystem.MDM), channelsContainer);
        return info;
    }

    private Range<Instant> lastMonth() {
        ZonedDateTime end = clock.instant().atZone(ZoneId.systemDefault()).with(ChronoField.MILLI_OF_DAY, 0L).plusDays(1);
        ZonedDateTime start = end.minusMonths(1);
        return Range.openClosed(start.toInstant(), end.toInstant());
    }

    private void setReasonInfo(List<DataValidationStatus> validationStatuses, UsagePointValidationStatusInfo info) {
        Map<ValidationRule, Long> suspectRulesCount = new HashMap<>();
        Map<ValidationRule, Long> informativeRulesCount = new HashMap<>();
        Map<EstimationRule, Long> estimateRulesCount = new HashMap<>();
        validationStatuses.forEach(validationStatus -> {
            Collection<? extends ReadingQuality> readingQualities = validationStatus.getReadingQualities();

            // add estimation rule
            readingQualities.stream()
                    .map(ReadingQualityRecord.class::cast)
                    .filter(ReadingQualityRecord::hasEstimatedCategory)
                    .findFirst()
                    .flatMap(readingQuality -> estimationService.findEstimationRuleByQualityType(readingQuality.getType()))
                    .ifPresent(estimationRule -> addEstimationRule(estimateRulesCount, estimationRule));

            // add validation rules
            boolean isSuspect = readingQualities.stream().anyMatch(quality -> quality.getType().isSuspect());
            validationStatus.getOffendedRules().stream()
                    .filter(rule -> !isSuspect || ValidationAction.WARN_ONLY != rule.getAction())
                    .forEach(rule -> addValidationRule(isSuspect ? suspectRulesCount : informativeRulesCount, rule));
        });
        info.suspectReason = createValidationRuleInfo(suspectRulesCount);
        info.informativeReason = createValidationRuleInfo(informativeRulesCount);
        info.estimateReason = createEstimationRuleInfo(estimateRulesCount);
    }

    private Set<ValidationRuleInfoWithNumber> createValidationRuleInfo(Map<ValidationRule, Long> rulesCount) {
        return rulesCount.entrySet()
                .stream()
                .map(this::getValidationRuleWithNumberSimpleInfo)
                .sorted(Comparator.comparing(validationRuleInfo -> validationRuleInfo.key.displayName))
                .collect(Collectors.toSet());
    }

    private Set<EstimationRuleInfoWithNumber> createEstimationRuleInfo(Map<EstimationRule, Long> rulesCount) {
        return rulesCount.entrySet()
                .stream()
                .map(this::getEstimationRuleWithNumberSimpleInfo)
                .sorted(Comparator.comparing(estimationRuleInfo -> estimationRuleInfo.key.estimatorName))
                .collect(Collectors.toSet());
    }

    private void addValidationRule(Map<ValidationRule, Long> validationRulesCount, ValidationRule validationRule) {
        validationRulesCount.putIfAbsent(validationRule, 0L);
        validationRulesCount.compute(validationRule, (k, v) -> v + 1);
    }

    private void addEstimationRule(Map<EstimationRule, Long> estimationRulesCount, EstimationRule estimationRule) {
        estimationRulesCount.putIfAbsent(estimationRule, 0L);
        estimationRulesCount.compute(estimationRule, (k, v) -> v + 1);
    }

    private ValidationRuleInfoWithNumber getValidationRuleWithNumberSimpleInfo(Map.Entry<ValidationRule, Long> validationRuleWithNumberEntry) {
        ValidationRuleInfoWithNumber info = new ValidationRuleInfoWithNumber();
        info.key = new ValidationRuleInfo();
        info.key.id = validationRuleWithNumberEntry.getKey().getId();
        info.key.displayName = validationRuleWithNumberEntry.getKey().getName();
        info.key.ruleSetVersion = new ValidationRuleSetVersionInfo();
        info.key.ruleSetVersion.id = validationRuleWithNumberEntry.getKey().getRuleSetVersion().getId();
        info.key.ruleSetVersion.ruleSet = new ValidationRuleSetInfo();
        info.key.ruleSetVersion.ruleSet.id = validationRuleWithNumberEntry.getKey().getRuleSet().getId();
        info.value = validationRuleWithNumberEntry.getValue();
        return info;
    }

    private EstimationRuleInfoWithNumber getEstimationRuleWithNumberSimpleInfo(Map.Entry<EstimationRule, Long> estimationRuleWithNumberEntry) {
        EstimationRuleInfoWithNumber info = new EstimationRuleInfoWithNumber();
        info.key = new EstimationRuleInfo();
        info.key.id = estimationRuleWithNumberEntry.getKey().getId();
        info.key.estimatorName = estimationRuleWithNumberEntry.getKey().getName();
        info.key.ruleSetId = estimationRuleWithNumberEntry.getKey().getRuleSet().getId();
        info.value = estimationRuleWithNumberEntry.getValue();
        return info;
    }

}
