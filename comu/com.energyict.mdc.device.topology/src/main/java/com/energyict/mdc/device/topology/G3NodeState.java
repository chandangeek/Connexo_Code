package com.energyict.mdc.device.topology;

import com.elster.jupiter.nls.TranslationKey;

import java.util.stream.Stream;

public enum G3NodeState implements TranslationKey {

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

    G3NodeState(int id, String key, String defaultFormat) {
        this.id = id;
        this.key = key;
        this.defaultFormat = defaultFormat;
    }

    public final int getId() {
        return this.id;
    }

    /**
     * Returns the G3NodeState that is uniquely identifier
     * by the specified ID.
     *
     * @param id The ID.
     * @return The corresponding G3NodeState, <code>null</code> if none matching.
     */
    public static G3NodeState fromId(int id) {
        return Stream
                .of(G3NodeState.values())
                .filter(p -> p.getId() == id)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown G3NodeState id " + id));
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