/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.AggregatedChannel;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.MetrologyContractChannelsContainer;
import com.elster.jupiter.util.streams.Functions;
import com.elster.jupiter.validation.EventType;
import com.elster.jupiter.validation.ValidationContext;
import com.elster.jupiter.validation.ValidationRuleSet;

import com.google.common.collect.Range;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.streams.Functions.asStream;
import static java.util.Comparator.naturalOrder;

/**
 * Manages validation for the channels container by (all) applicable rule sets.
 *
 * @see ChannelsContainerValidation
 */
class ChannelsContainerValidationList {
    private final ValidationServiceImpl validationService;
    private final EventService eventService;
    private ChannelsContainer channelsContainer;
    private List<ChannelsContainerValidation> channelsContainerValidations;

    @Inject
    ChannelsContainerValidationList(ValidationServiceImpl validationService,
                                            EventService eventService) {
        this.validationService = validationService;
        this.eventService = eventService;
    }

    ChannelsContainerValidationList ofActivePersistedValidations(ChannelsContainer channelsContainer) {
        return init(channelsContainer, validationService.getActivePersistedChannelsContainerValidations(channelsContainer));
    }

    ChannelsContainerValidationList ofUpdatedValidations(ValidationContext validationContext) {
        return init(validationContext.getChannelsContainer(), validationService.getUpdatedChannelsContainerValidations(validationContext));
    }

    private ChannelsContainerValidationList init(ChannelsContainer channelsContainer,
                                                 List<ChannelsContainerValidation> channelsContainerValidations) {
        this.channelsContainer = channelsContainer;
        this.channelsContainerValidations = Collections.unmodifiableList(channelsContainerValidations);
        return this;
    }

    void validate() {
        if (channelsContainer instanceof MetrologyContractChannelsContainer) {
            ((MetrologyContractChannelsContainer) channelsContainer).getMetrologyContract().sortReadingTypesByDependencyLevel()
                    .forEach(readingTypes -> {
                        Set<Channel> channels = readingTypes.stream()
                                .map(channelsContainer::getChannel)
                                .flatMap(Functions.asStream())
                                .collect(Collectors.toSet());
                        validate(channels);
                    });
        } else {
            Map<Channel, Range<Instant>> validationScopeByChannelMap = channelsContainer.getChannels().stream()
                    .collect(Collectors.toMap(Function.identity(), this::getValidationScope));
            channelsContainerValidations.forEach(ChannelsContainerValidation::validate);
            eventService.postEvent(EventType.VALIDATION_PERFORMED.topic(), new ValidationScopeImpl(channelsContainer, validationScopeByChannelMap));
        }
    }

    void validate(Set<Channel> channels) {
        Map<Channel, Range<Instant>> validationScopeByChannelMap = channels.stream()
                .collect(Collectors.toMap(Function.identity(), this::getValidationScope));
        channelsContainerValidations.forEach(channelsContainerValidation -> channelsContainerValidation.validate(channels));
        eventService.postEvent(EventType.VALIDATION_PERFORMED.topic(), new ValidationScopeImpl(channelsContainer, validationScopeByChannelMap));
    }

    private Range<Instant> getValidationScope(Channel channel) {
        return Range.greaterThan(getLastChecked(channel).orElse(channelsContainer.getStart()));
    }

    /**
     * Only updates the lastChecked in memory!!! For performance optimization COPL-882.
     *
     * @param rangeByChannelIdMap: Map of channelId-range to move the last checked before.
     * Channel must be identified by id here because there can be {@link AggregatedChannel}
     * that is just a wrapping on {@link Channel} with the same id.
     */
    void moveLastCheckedBefore(Map<Long, Range<Instant>> rangeByChannelIdMap) {
        channelsContainerValidations.forEach(channelsContainerValidation -> channelsContainerValidation.moveLastCheckedBefore(rangeByChannelIdMap));
    }

    void moveLastCheckedBefore(Instant date) {
        channelsContainerValidations.forEach(channelsContainerValidation -> channelsContainerValidation.moveLastCheckedBefore(date));
    }

    void updateLastChecked(Instant instant) {
        channelsContainerValidations.stream()
                .filter(ChannelsContainerValidation::isActive)
                .forEach(channelsContainerValidation -> channelsContainerValidation.updateLastChecked(instant));
    }

    void activate() {
        channelsContainerValidations.forEach(channelsContainerValidation -> {
            channelsContainerValidation.activate();
            channelsContainerValidation.save();
        });
    }

    boolean isActive() {
        return channelsContainerValidations.stream().anyMatch(ChannelsContainerValidation::isActive);
    }

    void updateLastChecked(Channel channel, Instant date) {
        channelValidationsFor(channel).updateLastChecked(date);
    }

    void moveLastCheckedBefore(Channel channel, Instant date) {
        channelValidationsFor(channel).moveLastCheckedBefore(date);
    }

    boolean isValidationActive(Channel channel) {
        return channelValidationsFor(channel).isValidationActive();
    }

    Optional<Instant> getLastChecked() {
        return channelsContainerValidations.stream()
                .filter(ChannelsContainerValidation::isActive)
                .filter(channelsContainerValidation -> channelsContainerValidation.getChannelValidations().stream().anyMatch(ChannelValidation::hasActiveRules))
                .map(ChannelsContainerValidation::getMinLastChecked)
                .filter(Objects::nonNull)
                .min(naturalOrder());
    }

    Optional<Instant> getLastValidationRun() {
        return channelsContainerValidations.stream()
                .filter(ChannelsContainerValidation::isActive)
                .filter(channelsContainerValidation -> channelsContainerValidation.getChannelValidations().stream().anyMatch(ChannelValidation::hasActiveRules))
                .map(ChannelsContainerValidation::getLastRun)
                .filter(Objects::nonNull)
                .min(naturalOrder());
    }

    Optional<Instant> getLastChecked(Channel channel) {
        return channelValidationsFor(channel).getLastChecked();
    }

    boolean isAllDataValidated() {
        return !channelsContainerValidations.isEmpty() && channelsContainerValidations.stream().allMatch(ChannelsContainerValidation::isAllDataValidated);
    }

    ChannelValidationContainer channelValidationsFor(Channel channel) {
        return ChannelValidationContainer.of(getChannelValidations(channel));
    }

    private List<ChannelValidation> getChannelValidations(Channel channel) {
        return channelsContainerValidations.stream()
                .map(channelsContainerValidation -> channelsContainerValidation.getChannelValidation(channel))
                .flatMap(asStream())
                .collect(Collectors.toList());
    }

    List<ValidationRuleSet> ruleSets() {
        return channelsContainerValidations.stream()
                .map(ChannelsContainerValidation::getRuleSet)
                .collect(Collectors.toList());
    }

    public void update() {
        channelsContainerValidations.forEach(ChannelsContainerValidation::save);
    }
}

