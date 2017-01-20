package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.cbo.QualityCodeIndex;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.mdm.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.CimChannel;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.util.streams.Functions;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.rest.ValidationRuleInfo;
import com.elster.jupiter.validation.rest.ValidationRuleSetInfo;
import com.elster.jupiter.validation.rest.ValidationRuleSetVersionInfo;

import com.google.common.collect.Ordering;
import com.google.common.collect.Range;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ValidationStatusFactory {
    private final Clock clock;
    private final ValidationService validationService;
    private final UsagePointConfigurationService usagePointConfigurationService;

    @Inject
    public ValidationStatusFactory(Clock clock, ValidationService validationService, UsagePointConfigurationService usagePointConfigurationService) {
        this.clock = clock;
        this.validationService = validationService;
        this.usagePointConfigurationService = usagePointConfigurationService;
    }

    public UsagePointValidationStatusInfo getValidationStatusInfo(EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration, MetrologyContract metrologyContract, List<Channel> channels) {
        UsagePointValidationStatusInfo info = new UsagePointValidationStatusInfo();
        Optional<ChannelsContainer> channelsContainer = effectiveMetrologyConfiguration.getChannelsContainer(metrologyContract);
        if (channelsContainer.isPresent()) {
            ValidationEvaluator validationEvaluator = validationService.getEvaluator();
            List<DataValidationStatus> validationStatuses = getDataValidationStatuses(validationEvaluator, channels);
            if (metrologyContract.getStatus(effectiveMetrologyConfiguration.getUsagePoint()).isComplete()) {
                info.validationActive = isValidationActive(metrologyContract, channels);
                info.lastChecked = getLastCheckedForChannels(validationEvaluator, channelsContainer.get(), channels);
                info.suspectReason = getSuspectReasonInfo(validationStatuses);
                info.allDataValidated = allDataValidated(validationEvaluator, channels);
                info.hasSuspects = hasSuspects(channels, channelsContainer.get().getRange());
            }
        }
        return info;
    }

    private List<DataValidationStatus> getDataValidationStatuses(ValidationEvaluator validationEvaluator, List<Channel> channels) {
        List<CimChannel> allOutputsAsCimChannels = channels.stream()
                .map(channel -> channel.getCimChannel(channel.getMainReadingType())) // All channels for usage point have only main reading type, see AggregatedChannelImpl#getBulkQuantityReadingType()
                .flatMap(Functions.asStream())
                .collect(Collectors.toList());
        return validationEvaluator.getValidationStatus(EnumSet.of(QualityCodeSystem.MDM), allOutputsAsCimChannels, Collections.emptyList(), lastMonth());
    }

    private Range<Instant> lastMonth() {
        ZonedDateTime end = clock.instant().atZone(ZoneId.systemDefault()).with(ChronoField.MILLI_OF_DAY, 0L).plusDays(1);
        ZonedDateTime start = end.minusMonths(1);
        return Range.openClosed(start.toInstant(), end.toInstant());
    }

    public Instant getLastCheckedForChannels(ValidationEvaluator validationEvaluator, ChannelsContainer channelsContainer, List<Channel> channels) {
        return channels.stream()
                .map(channel -> validationEvaluator.getLastChecked(channelsContainer, channel.getMainReadingType()))
                .filter(Objects::nonNull)
                .flatMap(Functions.asStream())
                .min(Ordering.natural())
                .orElse(null);
    }

    private Set<ValidationRuleInfoWithNumber> getSuspectReasonInfo(List<DataValidationStatus> validationStatuses) {
        Map<ValidationRule, Long> validationRulesCount = new HashMap<>();
        validationStatuses
                .stream()
                .forEach(validationStatus -> {
                    validationStatus.getOffendedRules().forEach(rule -> addSuspectValidationRule(validationRulesCount, rule));
                    validationStatus.getBulkOffendedRules().forEach(rule -> addSuspectValidationRule(validationRulesCount, rule));
                });
        return validationRulesCount.entrySet()
                .stream()
                .map(this::getValidationRuleWithNumberSimpleInfo)
                .sorted(Comparator.comparing(validationRuleInfo -> validationRuleInfo.key.displayName))
                .collect(Collectors.toSet());
    }

    private void addSuspectValidationRule(Map<ValidationRule, Long> validationRulesCount, ValidationRule validationRule) {
        validationRulesCount.putIfAbsent(validationRule, 0L);
        validationRulesCount.compute(validationRule, (k, v) -> v + 1);
    }

    private ValidationRuleInfoWithNumber getValidationRuleWithNumberSimpleInfo(Map.Entry<ValidationRule, Long> validationRuleWithNumberEntry) {
        ValidationRuleInfoWithNumber info = new ValidationRuleInfoWithNumber();
        info.key = new ValidationRuleInfo();
        info.key.id = validationRuleWithNumberEntry.getKey().getId();
        info.key.displayName = validationRuleWithNumberEntry.getKey().getDisplayName();
        info.key.ruleSetVersion = new ValidationRuleSetVersionInfo();
        info.key.ruleSetVersion.id = validationRuleWithNumberEntry.getKey().getRuleSetVersion().getId();
        info.key.ruleSetVersion.ruleSet = new ValidationRuleSetInfo();
        info.key.ruleSetVersion.ruleSet.id = validationRuleWithNumberEntry.getKey().getRuleSet().getId();
        info.value = validationRuleWithNumberEntry.getValue();
        return info;
    }

    private boolean allDataValidated(ValidationEvaluator validationEvaluator, List<Channel> channels) {
        return validationEvaluator.isAllDataValidated(channels);
    }

    public boolean isValidationActive(MetrologyContract metrologyContract, List<Channel> channels) {
        return channels.stream()
                .map(Channel::getMainReadingType)
                .anyMatch(readingType -> usagePointConfigurationService.getValidationRuleSets(metrologyContract).stream()
                        .flatMap(validationRuleSet -> validationRuleSet.getRules().stream())
                        .anyMatch(rule -> rule.isActive() && rule.appliesTo(readingType)));
    }

    public boolean hasSuspects(List<Channel> channels, Range<Instant> range) {
        return channels.stream()
                .anyMatch(channel -> channel.findReadingQualities()
                        .ofQualitySystems(EnumSet.of(QualityCodeSystem.MDM))
                        .ofQualityIndex(QualityCodeIndex.SUSPECT)
                        .inTimeInterval(range)
                        .actual()
                        .findFirst()
                        .isPresent());
    }
}
