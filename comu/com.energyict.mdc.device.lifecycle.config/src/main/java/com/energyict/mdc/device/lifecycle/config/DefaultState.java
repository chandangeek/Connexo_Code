package com.energyict.mdc.device.lifecycle.config;

import com.elster.jupiter.fsm.State;
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
public enum DefaultState  {

    IN_STOCK("dlc.default.inStock"),
    COMMISSIONING("dlc.default.commissioning"),
    ACTIVE("dlc.default.active"),
    INACTIVE("dlc.default.inactive"),
    DECOMMISSIONED("dlc.default.decommissioned"),
    REMOVED("dlc.default.removed");

    private final String key;

    DefaultState(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
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