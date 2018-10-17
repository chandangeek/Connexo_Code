package com.energyict.mdc.device.topology;

import java.util.stream.Stream;

public enum G3NodeState {

    UNKNOWN(0),
    NOT_ASSOCIATED(1),
    AVAILABLE(2),
    VANISHED(3),
    BLACKLISTED(4);

    private final int id;

    G3NodeState(int id) {
        this.id = id;
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

}