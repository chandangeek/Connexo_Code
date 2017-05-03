/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl;

import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public class ChannelValidationContainer {

    private final List<? extends ChannelValidation> channelValidations;

    private ChannelValidationContainer(List<? extends ChannelValidation> channelValidations) {
        this.channelValidations = channelValidations;
    }

    static ChannelValidationContainer of(List<? extends ChannelValidation> channelValidations) {
        return new ChannelValidationContainer(channelValidations);
    }

    void updateLastChecked(Instant date) {
        channelValidations.stream()
                .filter(ChannelValidation::hasActiveRules)
                .forEach(channelValidation -> {
                    if (channelValidation.updateLastChecked(date)) {
                        channelValidation.getChannelsContainerValidation().save();
                    }
                });
    }

    void moveLastCheckedBefore(Instant date) {
        channelValidations.stream()
                .filter(ChannelValidation::hasActiveRules)
                .forEach(channelValidation -> {
                    if (channelValidation.moveLastCheckedBefore(date)) {
                        channelValidation.getChannelsContainerValidation().save();
                    }
                });
    }

    boolean isValidationActive() {
        return stream().anyMatch(ChannelValidation::hasActiveRules);
    }

    Optional<Instant> getLastChecked() {
        return getLastChecked(channelValidations);
    }

    static Optional<Instant> getLastChecked(Collection<? extends ChannelValidation> validations) {
        Optional<Instant> lastChecked = getLastCheckedForCompleteRanges(validations.stream());
        if (lastChecked.isPresent()) {
            return lastChecked;
        } else {
            return getLastChecked(validations.stream());
        }
    }

    static Optional<Instant> getLastChecked(Stream<? extends ChannelValidation> validations) {
        // if any is null, then we should return Optional.empty()
        return validations
                .filter(ChannelValidation::hasActiveRules)
                .map(ChannelValidation::getLastChecked)
                .map(instant -> instant == null ? Instant.MIN : instant)
                .min(Comparator.naturalOrder())
                .flatMap(instant -> Instant.MIN.equals(instant) ? Optional.empty() : Optional.of(instant));
    }

    static Optional<Instant> getLastCheckedForCompleteRanges(Stream<? extends ChannelValidation> validations) {
        // if any is null, then we should return Optional.empty()
        return validations
                .filter(ChannelValidation::hasActiveRules)
                .filter(ChannelValidationContainer::isIncompleteRange)
                .map(ChannelValidation::getLastChecked)
                .map(instant -> instant == null ? Instant.MIN : instant)
                .min(Comparator.naturalOrder())
                .flatMap(instant -> Instant.MIN.equals(instant) ? Optional.empty() : Optional.of(instant));
    }

    static boolean isIncompleteRange(ChannelValidation channelValidation) {
        if (channelValidation.getChannelsContainerValidation().getRange().hasUpperBound() && channelValidation.getLastChecked() != null) {
            return channelValidation.getLastChecked().isBefore(channelValidation.getChannelsContainerValidation().getRange().upperEndpoint());
        } else {
            return true;
        }
    }

    boolean isEmpty() {
        return channelValidations.isEmpty();
    }

    public Stream<ChannelValidation> stream() {
        return channelValidations.stream().map(Function.identity());
    }
}
