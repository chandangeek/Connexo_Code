/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.validation.ValidationRuleSet;

import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.streams.Functions.asStream;
import static java.util.Comparator.naturalOrder;

/**
 * Manages validation for the channels container by (all) applicable rule sets.
 *
 * @see ChannelsContainerValidation
 */
class ChannelsContainerValidationList {

    private final List<ChannelsContainerValidation> channelsContainerValidations;

    private ChannelsContainerValidationList(List<ChannelsContainerValidation> channelsContainerValidations) {
        this.channelsContainerValidations = Collections.unmodifiableList(channelsContainerValidations);
    }

    static ChannelsContainerValidationList of(List<ChannelsContainerValidation> channelsContainerValidations) {
        return new ChannelsContainerValidationList(channelsContainerValidations);
    }

    void validate() {
        channelsContainerValidations.forEach(ChannelsContainerValidation::validate);
    }

    public void validate(ReadingType readingType) {
        channelsContainerValidations.forEach(channelsContainerValidation -> channelsContainerValidation.validate(readingType));
    }

    void moveLastCheckedBefore(Map<Channel, Range<Instant>> ranges) {
        channelsContainerValidations.forEach(channelsContainerValidation -> channelsContainerValidation.moveLastCheckedBefore(ranges));
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
                .map(ChannelValidation.class::cast)
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

