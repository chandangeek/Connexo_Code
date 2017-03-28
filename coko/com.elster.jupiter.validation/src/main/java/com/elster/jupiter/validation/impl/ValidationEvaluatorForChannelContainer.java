/**
 * Copyright 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.validation.impl;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.ReadingContainer;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.validation.ValidationContextImpl;
import com.elster.jupiter.validation.ValidationRuleSet;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsLast;

public class ValidationEvaluatorForChannelContainer extends AbstractValidationEvaluator {

    private final ValidationServiceImpl validationService;
    private final ChannelsContainer channelsContainer;

    private ChannelsContainerValidationList validationContainer;
    private Multimap<String, IValidationRule> mapQualityToRule;

    ValidationEvaluatorForChannelContainer(ValidationServiceImpl validationService, ChannelsContainer container) {
        this.validationService = validationService;
        this.channelsContainer = container;
    }

    private ChannelsContainerValidationList getValidationContainer() {
        if (validationContainer == null) {
            validationContainer = validationService.updatedChannelsContainerValidationsFor(new ValidationContextImpl(channelsContainer));
        }
        return validationContainer;
    }

    @Override
    ChannelValidationContainer getChannelValidationContainer(Channel channel) {

        return getValidationContainer().channelValidationsFor(channel);
    }

    @Override
    Multimap<String, IValidationRule> getMapQualityToRule(ChannelValidationContainer channelValidations) {
        if (mapQualityToRule == null) {
            mapQualityToRule = initRulesPerQuality();
        }
        return mapQualityToRule;
    }

    private ImmutableListMultimap<String, IValidationRule> initRulesPerQuality() {
        Set<IValidationRule> rules = getValidationContainer().ruleSets().stream()
                .distinct()
                .map(ValidationRuleSet::getRules)
                .flatMap(List::stream)
                .map(IValidationRule.class::cast)
                .collect(Collectors.toSet());
        return Multimaps.index(rules, i -> i.getReadingQualityType().getCode());
    }

    @Override
    public boolean isAllDataValidated(ChannelsContainer channelsContainer) {
        if (this.channelsContainer.equals(channelsContainer)) {
            return getValidationContainer().isAllDataValidated();
        } else {
            throw new IllegalArgumentException("Cannot check isAllDataValidated for channelsContainer " + channelsContainer + " on evaluator for channelsContainer " + this.channelsContainer);
        }
    }

    @Override
    public boolean isAllDataValidated(List<Channel> channels) {
        Comparator<Instant> comparator = nullsLast(naturalOrder());
        for (Channel channel : channels) {
            ChannelValidationContainer channelValidationContainer = getValidationContainer().channelValidationsFor(channel);
            if (!channelValidationContainer.isValidationActive() || channelValidationContainer.getLastChecked()
                    .map(lastChecked -> comparator.compare(lastChecked, channel.getLastDateTime()) < 0)
                    .orElse(true)) {
                return false;
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
        return getValidationContainer().channelValidationsFor(channel).isValidationActive();
    }

    @Override
    public boolean isValidationEnabled(ReadingContainer meter, ReadingType readingType) {
        if (channelsContainer.getChannel(readingType).isPresent() && meter.getChannelsContainers().contains(this.channelsContainer)) {
            return getValidationContainer().channelValidationsFor(channelsContainer.getChannel(readingType).get()).isValidationActive();
        } else {
            throw new IllegalArgumentException("Cannot check if validation is enabled for reading container " + meter + " and reading type " + readingType + " on ValidationEvaluator for channelscontainer" + channelsContainer);
        }
    }

    @Override
    public Optional<Instant> getLastChecked(ReadingContainer meter, ReadingType readingType) {
        if (channelsContainer.getChannel(readingType).isPresent() && meter.getChannelsContainers().contains(this.channelsContainer)) {
            return getValidationContainer().channelValidationsFor(channelsContainer.getChannel(readingType).get()).getLastChecked();
        } else {
            throw new IllegalArgumentException("Cannot get lastChecked for reading container " + meter + " and reading type " + readingType + " on ValidationEvaluator for channelscontainer" + channelsContainer);
        }
    }

}
