package com.elster.jupiter.servicecalls;

import com.elster.jupiter.fsm.State;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Models the {@link State}s
 * that are part of the default {@link ServiceCallLifeCycle}.
 */
public enum DefaultState {
    CREATED("sclc.default.created"),
    PENDING("sclc.default.pending"),
    SCHEDULED("sclc.default.scheduled"),
    ONGOING("sclc.default.ongoing"),
    PAUSED("sclc.default.paused"),
    WAITING("sclc.default.waiting"),
    PARTIAL_SUCCESS("sclc.default.partialSuccess"),
    SUCCESSFUL("sclc.default.successful"),
    FAILED("sclc.default.failed"),
    REJECTED("sclc.default.rejected"),
    CANCELLED("sclc.default.cancelled");

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

}