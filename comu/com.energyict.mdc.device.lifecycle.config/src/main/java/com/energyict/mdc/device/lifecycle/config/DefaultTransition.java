/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.lifecycle.config;

import com.elster.jupiter.fsm.State;

import java.util.Optional;
import java.util.stream.Stream;

public enum DefaultTransition {

    COMMISSION(DefaultState.IN_STOCK, DefaultState.COMMISSIONING),
    INSTALL_AND_ACTIVATE_WITHOUT_COMMISSIONING(DefaultState.IN_STOCK, DefaultState.ACTIVE),
    INSTALL_INACTIVE_WITHOUT_COMMISSIONING(DefaultState.IN_STOCK, DefaultState.INACTIVE),
    INSTALL_AND_ACTIVATE(DefaultState.COMMISSIONING, DefaultState.ACTIVE),
    INSTALL_INACTIVE(DefaultState.COMMISSIONING, DefaultState.INACTIVE),
    ACTIVATE(DefaultState.INACTIVE, DefaultState.ACTIVE),
    DEACTIVATE(DefaultState.ACTIVE, DefaultState.INACTIVE),
    DEACTIVATE_AND_DECOMMISSION(DefaultState.ACTIVE, DefaultState.DECOMMISSIONED),
    DECOMMISSION(DefaultState.INACTIVE, DefaultState.DECOMMISSIONED),
    DELETE_FROM_DECOMMISSIONED(DefaultState.DECOMMISSIONED, DefaultState.REMOVED),
    DELETE_FROM_IN_STOCK(DefaultState.IN_STOCK, DefaultState.REMOVED),

    ;

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

    public static Optional<DefaultTransition> getDefaultTransition(State fromState, State toState) {
        return fromState == null || toState == null ? Optional.empty() : Stream.of(values())
                .filter(candidate -> isDefaultState(candidate.getFromState(), fromState) &&
                        isDefaultState(candidate.getToState(), toState))
                .findFirst();
    }

    private static boolean isDefaultState(DefaultState defaultState, State state) {
        return !state.isCustom() && state.getName().equals(defaultState.getKey());
    }
}