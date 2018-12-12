/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology;

import com.elster.jupiter.nls.TranslationKey;

import java.util.stream.Stream;

/**
 * Models the known and supported PLC modulation schemes.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-15 (13:16)
 */
public enum ModulationScheme implements TranslationKey {

    DIFFERENTIAL(0, "modulationScheme.differential", "Differential"),
    COHERENT(1, "modulationScheme.coherent", "Coherent");

    private final int id;
    private final String key;
    private final String defaultFormat;

    public final int getId() {
        return this.id;
    }

    ModulationScheme(int id, String key, String defaultFormat) {
        this.id = id;
        this.key = key;
        this.defaultFormat = defaultFormat;
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

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }

}