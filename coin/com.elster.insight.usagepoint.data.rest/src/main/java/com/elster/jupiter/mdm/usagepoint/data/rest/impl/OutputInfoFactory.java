package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.mdm.common.rest.TimeDurationInfo;
import com.elster.jupiter.mdm.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.validation.rest.ValidationRuleInfo;

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
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class OutputInfoFactory {
    private final Clock clock;
    private final ValidationService validationService;
    private final UsagePointConfigurationService usagePointConfigurationService;

    @Inject
    public OutputInfoFactory(Clock clock, ValidationService validationService, UsagePointConfigurationService usagePointConfigurationService) {
        this.clock = clock;
        this.validationService = validationService;
        this.usagePointConfigurationService = usagePointConfigurationService;
    }

    public OutputInfo asInfo(ReadingTypeDeliverable readingTypeDeliverable) {
        OutputInfo outputInfo = new OutputInfo();
        TimeDuration timeDuration = null;
        ReadingType readingType = readingTypeDeliverable.getReadingType();
        MacroPeriod macroPeriod = readingType.getMacroPeriod();
        TimeAttribute measuringPeriod = readingType.getMeasuringPeriod();
        outputInfo.id = readingTypeDeliverable.getId();
        outputInfo.name = readingTypeDeliverable.getName();
        outputInfo.readingType = new ReadingTypeInfo(readingType);
        outputInfo.formula = readingTypeDeliverable.getFormula() != null ? FormulaInfo.asInfo(readingTypeDeliverable.getFormula()) : null;
        if (!measuringPeriod.equals(TimeAttribute.NOTAPPLICABLE)) {
            timeDuration = TimeDuration.minutes(measuringPeriod.getMinutes());
        } else if (macroPeriod.equals(MacroPeriod.DAILY)) {
            timeDuration = TimeDuration.days(1);
        } else if (macroPeriod.equals(MacroPeriod.MONTHLY)) {
            timeDuration = TimeDuration.months(1);
        } else if (macroPeriod.equals(MacroPeriod.WEEKLYS)) {
            timeDuration = TimeDuration.weeks(1);
        }
        outputInfo.interval = new TimeDurationInfo(timeDuration);
        outputInfo.flowUnit = ReadingTypeUnitConversion.isFlowUnit(readingType.getUnit()) ? "flow" : "volume";
        return outputInfo;
    }

    public OutputInfo fullInfo(ReadingTypeDeliverable readingTypeDeliverable, EffectiveMetrologyConfigurationOnUsagePoint effectiveMetrologyConfiguration, MetrologyContract metrologyContract) {
        OutputInfo outputInfo = asInfo(readingTypeDeliverable);
        Optional<ChannelsContainer> channelsContainer = effectiveMetrologyConfiguration.getChannelsContainer(metrologyContract);
        Optional<Channel> outputChannel = channelsContainer.flatMap(container -> container.getChannel(readingTypeDeliverable.getReadingType()));
        if (outputChannel.isPresent()) {
            ValidationEvaluator validationEvaluator = validationService.getEvaluator();
            outputInfo.validationInfo = new UsagePointChannelValidationInfo();
            validationEvaluator.getLastChecked(channelsContainer.get(), readingTypeDeliverable.getReadingType())
                    .ifPresent(lastChecked -> outputInfo.validationInfo.lastChecked = lastChecked);
            Map<ValidationRule, Long> validationRulesCount = new HashMap<>();
            validationEvaluator.getValidationStatus(EnumSet.of(QualityCodeSystem.MDM), outputChannel.get(), Collections.emptyList(), lastMonth())
                    .stream()
                    .forEach(validationStatus -> {
                        validationStatus.getOffendedRules().forEach(rule -> addSuspectValidationRule(validationRulesCount, rule));
                        validationStatus.getBulkOffendedRules().forEach(rule -> addSuspectValidationRule(validationRulesCount, rule));
                    });
            outputInfo.validationInfo.suspectReason = validationRulesCount.entrySet()
                    .stream()
                    .map(entry -> {
                        ValidationRuleInfoWithNumber info = new ValidationRuleInfoWithNumber();
                        info.key = new ValidationRuleInfo();
                        info.key.id = entry.getKey().getId();
                        info.key.displayName = entry.getKey().getDisplayName();
                        info.value = entry.getValue();
                        return info;
                    })
                    .sorted(Comparator.comparing(info -> info.key.name))
                    .collect(Collectors.toSet());
            outputInfo.validationInfo.allDataValidated = validationEvaluator.isAllDataValidated(channelsContainer.get());
            outputInfo.validationInfo.validationActive = metrologyContract.getStatus(effectiveMetrologyConfiguration.getUsagePoint()).isComplete()
                    && !usagePointConfigurationService.getValidationRuleSets(metrologyContract).isEmpty();
        }
        return outputInfo;
    }

    private void addSuspectValidationRule(Map<ValidationRule, Long> validationRulesCount, ValidationRule validationRule) {
        validationRulesCount.putIfAbsent(validationRule, 0L);
        validationRulesCount.compute(validationRule, (k, v) -> v + 1);
    }

    private Range<Instant> lastMonth() {
        ZonedDateTime end = clock.instant().atZone(ZoneId.systemDefault()).with(ChronoField.MILLI_OF_DAY, 0L).plusDays(1);
        ZonedDateTime start = end.minusMonths(1);
        return Range.openClosed(start.toInstant(), end.toInstant());
    }
}
