/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.ReadingContainer;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.validation.ValidationContextImpl;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.streams.Functions.asStream;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsLast;

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
        return validationService.getPersistedChannelsContainerValidations(channelsContainer)
                .stream()
                .allMatch(ChannelsContainerValidation::isAllDataValidated);
    }

    @Override
    public boolean isAllDataValidated(List<Channel> channels) {
        Comparator<Instant> comparator = nullsLast(naturalOrder());
        Map<Long, List<ChannelsContainerValidation>> channelsContainerValidations = new HashMap<>();
        for (Channel channel : channels) {
            ChannelsContainer channelsContainer = channel.getChannelsContainer();
            List<ChannelsContainerValidation> validations = channelsContainerValidations.get(channelsContainer.getId());
            if (validations == null || validations.isEmpty()) {
                validations = validationService.getPersistedChannelsContainerValidations(channelsContainer);
                if (validations == null || validations.isEmpty()) { // For case when validation never start before
                    validations = validationService.getUpdatedChannelsContainerValidations(new ValidationContextImpl(channelsContainer));
                }
                channelsContainerValidations.put(channelsContainer.getId(), validations);
            }
            if (validations == null || validations.isEmpty()) { // If there is no applicable ruleset, then data is not validated.
                return false;
            }
            for (ChannelsContainerValidation validation : validations) {
                if (validation.getLastRun() == null) { // if validation by that ruleset never run before
                    return false;
                }
                Optional<ChannelValidation> channelValidation = validation.getChannelValidation(channel);
                if (!channelValidation.isPresent()
                        || !channelValidation.get().hasActiveRules()
                        || comparator.compare(channelValidation.get().getLastChecked(), channel.getLastDateTime()) < 0) {
                    return false;
                }
            }
        }
        return true;
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
        return validationService.getChannelsContainerValidations(channel.getChannelsContainer())
                .stream()
                .map(channelsContainerValidation -> channelsContainerValidation.getChannelValidation(channel))
                .flatMap(asStream())
                .anyMatch(ChannelValidation::hasActiveRules);

    }

    @Override
    public boolean isValidationEnabled(ReadingContainer meter, ReadingType readingType) {
        return meter.getChannelsContainers().stream()
                .flatMap(channelsContainer -> channelsContainer.getChannels().stream())
                .filter(channel -> channel.getReadingTypes().contains(readingType))
                .anyMatch(validationService::isValidationActive);
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
