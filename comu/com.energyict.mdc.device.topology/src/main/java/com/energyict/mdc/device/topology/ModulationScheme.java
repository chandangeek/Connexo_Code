/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology;

import java.util.stream.Stream;

/**
 * Models the known and supported PLC modulation schemes.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-15 (13:16)
 */
public enum ModulationScheme {

    DIFFERENTIAL(0),
    COHERENT(1);

    private final int id;

    private ModulationScheme(int id) {
        this.id = id;
    }

    public final int getId() {
        return this.id;
    }

    /**
     * Returns the ModulationScheme that is uniquely identifier
     * by the specified ID.
     *
     * @param id The ID.
     * @return The coresponding scheme, <code>null</code> if none matching.
     */
    public static ModulationScheme fromId(int id) {
        return Stream
                .of(ModulationScheme.values())
                .filter(p -> p.getId() == id)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown ModulationScheme id " + id));
    }

}