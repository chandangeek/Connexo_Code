package com.elster.jupiter.validation.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.ReadingContainer;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.util.conditions.Where;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.streams.Functions.asStream;
import static java.util.Comparator.naturalOrder;

/**
 * Created by tgr on 5/09/2014.
 */
class ValidationEvaluatorImpl extends AbstractValidationEvaluator {

    private final ValidationServiceImpl validationService;

    ValidationEvaluatorImpl(ValidationServiceImpl validationService) {
        this.validationService = validationService;
    }

    @Override
    public boolean isAllDataValidated(ChannelsContainer channelsContainer) {
        return validationService.getStoredChannelsContainerValidations(channelsContainer).stream()
                .allMatch(ChannelsContainerValidation::isAllDataValidated);
    }

    @Override
    public boolean isValidationEnabled(Meter meter) {
        return validationService.validationEnabled(meter);
    }

    @Override
    public boolean isValidationOnStorageEnabled(Meter meter) {
        return validationService.validationOnStorageEnabled(meter);
    }

    @Override
    public boolean isValidationEnabled(Channel channel) {
        return validationService.getChannelsContainerValidations(channel.getChannelsContainer()).stream()
                .map(m -> m.getChannelValidation(channel))
                .flatMap(asStream())
                .anyMatch(ChannelValidation::hasActiveRules);

    }

    @Override
    public boolean isValidationEnabled(ReadingContainer meter, ReadingType readingType) {
        return (meter.getChannelsContainers().stream()
                .flatMap(m -> m.getChannels().stream())
                .filter(k -> k.getReadingTypes().contains(readingType))
                .filter(validationService::isValidationActive)).count() > 0;
    }

    @Override
    public Optional<Instant> getLastChecked(ReadingContainer meter, ReadingType readingType) {
        return meter.getChannelsContainers().stream()
                .flatMap(m -> m.getChannels().stream())
                .filter(k -> k.getReadingTypes().contains(readingType))
                .filter(validationService::isValidationActive)
                .map(validationService::getLastChecked)
                .flatMap(asStream())
                .max(naturalOrder());
    }

    @Override
    ChannelValidationContainer getChannelValidationContainer(Channel channel) {
        return ChannelValidationContainer.of(validationService.getChannelValidations(channel));
    }

    @Override
    Multimap<String, IValidationRule> getMapQualityToRule(ChannelValidationContainer channelValidations) {
        Query<IValidationRule> ruleQuery = validationService.getAllValidationRuleQuery();
        Set<IValidationRule> rules = channelValidations.stream()
                .map(ChannelValidation::getChannelsContainerValidation)
                .map(ChannelsContainerValidation::getRuleSet)
                .flatMap(ruleSet -> ruleQuery.select(Where.where("ruleSetVersion.ruleSet").isEqualTo(ruleSet)).stream())
                .collect(Collectors.toSet());
        return Multimaps.index(rules, i -> i.getReadingQualityType().getCode());
    }

}
