package com.energyict.mdc.device.topology;

import java.util.stream.Stream;

public enum State {

    UNKNOWN(0),
    NOT_ASSOCIATED(1),
    AVAILABLE(2),
    VANISHED(3),
    BLACKLISTED(4);

    private final int id;

    State(int id) {
        this.id = id;
    }

    public final int getId() {
        return this.id;
    }

    /**
     * Returns the State that is uniquely identifier
     * by the specified ID.
     *
     * @param id The ID.
     * @return The corresponding State, <code>null</code> if none matching.
     */
    public static State fromId(int id) {
        return Stream
                .of(State.values())
                .filter(p -> p.getId() == id)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown State id " + id));
    }

}