/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.config;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.streams.Functions;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Models the {@link com.elster.jupiter.fsm.State}s
 * that are part of the default {@link DeviceLifeCycle}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-12 (10:37)
 */
public enum DefaultState implements TranslationKey {

    IN_STOCK("dlc.default.inStock", "In stock"),
    COMMISSIONING("dlc.default.commissioning", "Commissioning"),
    ACTIVE("dlc.default.active", "Active"),
    INACTIVE("dlc.default.inactive", "Inactive"),
    DECOMMISSIONED("dlc.default.decommissioned", "Decommissioned"),
    REMOVED("dlc.default.removed", "Removed");

    private final String key;
    private final String defaultFormat;

    DefaultState(String key, String defaultFormat) {
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }

    /**
     * Determines the DefaultState for the specified {@link State}.
     * Will return <code>Optional.empty()</code> when the State
     * is not standard or the symbolic name does not match
     * one of the standards.
     *
     * @param state The State
     * @return The DefaultState
     */
    public static Optional<DefaultState> from(State state) {
        if (state != null && !state.isCustom()) {
            String symbolicName = state.getName();
            return Stream
                    .of(DefaultState.values())
                    .filter(d -> d.getKey().equals(symbolicName))
                    .findFirst();
        } else {
            return Optional.empty();
        }
    }

    public static Optional<DefaultState> fromKey(String key) {
        return Stream
                    .of(DefaultState.values())
                    .filter(s -> s.getKey().equals(key))
                    .findFirst();
    }

    public static Set<DefaultState> fromKeys(Set<String> keys) {
        Set<DefaultState> states =
            keys
                .stream()
                .map(DefaultState::fromKey)
                .flatMap(Functions.asStream())
                .collect(Collectors.toSet());
        if (!states.isEmpty()) {
            return EnumSet.copyOf(states);
        } else {
            return EnumSet.noneOf(DefaultState.class);
        }
    }

}