/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl;

import com.elster.jupiter.metering.Channel;

import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
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
        if (validations.stream().filter(ChannelValidation::hasActiveRules).map(ChannelValidation::getLastChecked).anyMatch(e -> e == null)) {
            return Optional.empty();
        }
        Map<Channel, List<ChannelValidation>> channelValidations = validations.stream().collect(Collectors.groupingBy(ChannelValidation::getChannel, Collectors.toList()));
        return channelValidations.values().stream().map(ChannelValidationContainer::getLastCheckedForChannel).filter(Optional::isPresent).map(Optional::get).min(Comparator.naturalOrder());
    }

    static Optional<Instant> getLastCheckedForChannel(List<ChannelValidation> channelValidations) {
        return Optional.ofNullable(channelValidations.stream()
                .filter(channelValidation -> channelValidation.getChannelsContainerValidation().isActive())
                .filter(ChannelValidation::hasActiveRules)
                .filter(channelValidation -> !channelValidation.isLastValidationComplete())
                .map(ChannelValidation::getLastChecked)
                .min(Comparator.naturalOrder()).orElseGet(() -> channelValidations.stream()
                        .filter(ChannelValidation::hasActiveRules)
                        .map(ChannelValidation::getLastChecked)
                        .max(Comparator.naturalOrder())
                        .orElse(null)));
    }

    boolean isEmpty() {
        return channelValidations.isEmpty();
    }

    public Stream<ChannelValidation> stream() {
        return channelValidations.stream().map(Function.identity());
    }
}
