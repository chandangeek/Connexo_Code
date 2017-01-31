/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.config;

import java.util.Optional;
import java.util.stream.Stream;

public enum DefaultTransition {
    INSTALL_ACTIVE(DefaultState.UNDER_CONSTRUCTION, DefaultState.ACTIVE),
    INSTALL_INACTIVE(DefaultState.UNDER_CONSTRUCTION, DefaultState.INACTIVE),
    DEACTIVATE(DefaultState.ACTIVE, DefaultState.INACTIVE),
    ACTIVATE(DefaultState.INACTIVE, DefaultState.ACTIVE),
    DEMOLISH_FROM_ACTIVE(DefaultState.ACTIVE, DefaultState.DEMOLISHED),
    DEMOLISH_FROM_INACTIVE(DefaultState.INACTIVE, DefaultState.DEMOLISHED);

    private final DefaultState from;
    private final DefaultState to;

    DefaultTransition(DefaultState from, DefaultState to) {
        this.from = from;
        this.to = to;
    }

    public DefaultState getFromState() {
        return this.from;
    }

    public DefaultState getToState() {
        return this.to;
    }

    public static Optional<DefaultTransition> getDefaultTransition(UsagePointTransition transition) {
        if (transition == null) {
            return Optional.empty();
        }
        return Stream.of(values())
                .filter(candidate -> transition.getFrom().isDefault(candidate.getFromState())
                        && transition.getTo().isDefault(candidate.getToState())
                        && !transition.getTriggeredBy().isPresent())
                .findFirst();
    }

    public static Optional<DefaultTransition> getDefaultTransition(UsagePointState fromState, UsagePointState toState) {
        if (fromState == null || toState == null) {
            return Optional.empty();
        }
        return Stream.of(values())
                .filter(candidate -> fromState.isDefault(candidate.getFromState()) && toState.isDefault(candidate.getToState()))
                .findFirst();
    }
}
