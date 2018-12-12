/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology;

import com.elster.jupiter.nls.TranslationKey;

import java.util.stream.Stream;

/**
 * Models the known and supported values for
 * the phase information of a {@link PLCNeighbor}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-15 (13:29)
 */
public enum State implements TranslationKey {

    /**
     * UNKNOWN.
     */
    UNKNOWN(0, "state.unknown", "Unknown"),

    /**
     * NOT_ASSOCIATED.
     */
    NOT_ASSOCIATED(1, "state.notAssociated", "Not associated"),

    /**
     * AVAILABLE
     */
    AVAILABLE(2, "state.available", "Available"),

    /**
     * VANISHED
     */
    VANISHED(3, "state.vanished", "Vanished"),

    /**
     * BLACKLISTED
     */
    BLACKLISTED(4, "state.blacklisted", "Blacklisted");


    private final int id;
    private final String key;
    private final String defaultFormat;

    public final int getId() {
        return this.id;
    }

    State(int id, String key, String defaultFormat) {
        this.id = id;
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    /**
     * Returns the State that is uniquely identified by the given Id.
     *
     * @param id The Id to match.
     * @return The matching PhaseInfo, UNKNOWN if not known.
     */
    public static State fromId(int id) {
        return Stream
                .of(State.values())
                .filter(p -> p.getId() == id)
                .findFirst()
                .orElse(State.UNKNOWN);
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }

}