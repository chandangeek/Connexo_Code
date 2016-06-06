package com.elster.jupiter.validation.impl;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class ChannelValidationContainer {

    private final List<? extends IChannelValidation> channelValidations;

    private ChannelValidationContainer(List<? extends IChannelValidation> channelValidations) {
        this.channelValidations = channelValidations;
    }

    static ChannelValidationContainer of(List<? extends IChannelValidation> channelValidations) {
        return new ChannelValidationContainer(channelValidations);
    }

    void updateLastChecked(Instant date) {
         channelValidations.stream()
             .filter(IChannelValidation::hasActiveRules)
             .forEach(cv -> {
                 cv.updateLastChecked(date);
                 cv.getMeterActivationValidation().save();
             });
    }

    boolean isValidationActive() {
        return channelValidations.stream().anyMatch(IChannelValidation::hasActiveRules);
    }

    // TODO: think of lastChecked, if it should be common for MDC & MDM, or calculated and set independently
    Optional<Instant> getLastChecked() {
        return getLastChecked(stream());
    }

    static Optional<Instant> getLastChecked(Stream<? extends IChannelValidation> validations) {
        // if any is null, then we should return Optional.empty()
        return validations
                .map(IChannelValidation::getLastChecked)
                .map(instant -> instant == null ? Instant.MIN : instant)
                .min(Comparator.naturalOrder())
                .flatMap(instant -> Instant.MIN.equals(instant) ? Optional.empty() : Optional.of(instant));
    }

    boolean isEmpty() {
        return channelValidations.isEmpty();
    }

    public Stream<? extends IChannelValidation> stream() {
        return channelValidations.stream();
    }
}
