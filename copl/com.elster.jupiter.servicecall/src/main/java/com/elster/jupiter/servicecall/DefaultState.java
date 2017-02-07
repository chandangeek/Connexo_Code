/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Models the {@link State}s
 * that are part of the default {@link ServiceCallLifeCycle}.
 */
public enum DefaultState implements TranslationKey {
    CREATED("sclc.default.created", "Created"),
    PENDING("sclc.default.pending", "Pending"),
    SCHEDULED("sclc.default.scheduled", "Scheduled"),
    ONGOING("sclc.default.ongoing", "Ongoing"),
    PAUSED("sclc.default.paused", "Paused"),
    WAITING("sclc.default.waiting", "Waiting"),
    PARTIAL_SUCCESS("sclc.default.partialSuccess", "Partial success"),
    SUCCESSFUL("sclc.default.successful", "Successful"),
    FAILED("sclc.default.failed", "Failed"),
    REJECTED("sclc.default.rejected", "Rejected"),
    CANCELLED("sclc.default.cancelled", "Cancelled");

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

    public boolean matches(State state) {
        return getKey().equals(state.getName());
    }
    public static Optional<DefaultState> from(String key) {
        return Stream
                .of(DefaultState.values())
                .filter(d -> d.getKey().equals(key))
                .findFirst();
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }

    public String getDisplayName(Thesaurus thesaurus) {
        return thesaurus.getFormat(this).format();
    }

}