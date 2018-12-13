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
    ROBO(0, ModulationScheme.DIFFERENTIAL),
    DBPSK(1, ModulationScheme.DIFFERENTIAL),
    DQPSK(2, ModulationScheme.DIFFERENTIAL),
    D8PSK(3, ModulationScheme.DIFFERENTIAL),
    QAM16(4, ModulationScheme.COHERENT),
    SUPERROBO(5, ModulationScheme.DIFFERENTIAL),
    C8PSK(6, ModulationScheme.COHERENT),
    CQPSK(7, ModulationScheme.COHERENT),
    CBPSK(8, ModulationScheme.COHERENT),
    UNKNOWN(99, ModulationScheme.COHERENT);

    private int id;
    private ModulationScheme modulationScheme;

    Modulation(int id, ModulationScheme modulationScheme) {
        this.id = id;
        this.modulationScheme = modulationScheme;
    }

    public final ModulationScheme getModulationScheme() {
        return this.modulationScheme;
    }

    public final int getId() {
        return this.id;
    }

    /**
     * Returns the ModulationScheme that is uniquely identifier
     * by the specified ordinal.
     *
     * @param id The ordinal.
     * @return The corresponding scheme if found.
     * @throws IllegalArgumentException if Modulation with give id is not found.
     */
    public static Modulation fromId(int id) {
        return Stream
                .of(Modulation.values())
                .filter(i -> i.id == id)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown Modulation id " + id));
    }
}