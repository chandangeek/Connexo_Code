/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology;

import java.util.stream.Stream;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-15 (14:01)
 */
public enum Modulation {
    D8PSK(ModulationScheme.DIFFERENTIAL),
    DQPSK(ModulationScheme.DIFFERENTIAL),
    DBPSK(ModulationScheme.DIFFERENTIAL),
    C8PSK(ModulationScheme.COHERENT),
    CQPSK(ModulationScheme.COHERENT),
    CBPSK(ModulationScheme.COHERENT);

    private ModulationScheme modulationScheme;

    Modulation(ModulationScheme modulationScheme) {
        this.modulationScheme = modulationScheme;
    }

    public final ModulationScheme getModulationScheme() {
        return this.modulationScheme;
    }


    /**
     * Returns the ModulationScheme that is uniquely identifier
     * by the specified ordinal.
     *
     * @param ordinal The ordinal.
     * @return The coresponding scheme, <code>null</code> if none matching.
     */
    public static Modulation fromOrdinal(int ordinal) {
        return Stream
                .of(Modulation.values())
                .filter(p -> p.ordinal() == ordinal)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown Modulation ordinal " + ordinal));
    }
}