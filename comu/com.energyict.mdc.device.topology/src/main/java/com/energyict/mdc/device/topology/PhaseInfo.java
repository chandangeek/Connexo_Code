/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology;

import java.util.stream.Stream;

/**
 * Models the known and supported values for
 * the phase information of a {@link PLCNeighbor}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-15 (13:29)
 */
public enum PhaseInfo {

    /**
     * In phase.
     */
    INPHASE(0),

    /**
     * 60 degrees out of phase.
     */
    DEGREE60(1),

    /**
     * 120 degrees out of phase.
     */
    DEGREE120(2),

    /**
     * 180 degrees out of phase.
     */
    DEGREE180(3),

    /**
     * 240 degrees out of phase.
     */
    DEGREE240(4),

    /**
     * 300 degrees out of phase.
     */
    DEGREE300(5),

    /**
     * Replacement for <code>null</code> and will/should
     * be used when phase information is missing.
     */
    NOPHASEINFO(7),

    UNKNOWN(99);

    private int id;

    private PhaseInfo(int id) {
        this.id = id;
    }

    public final int getId() {
        return this.id;
    }

    /**
     * Returns the PhaseInfo that is uniquely identified by the given Id.
     *
     * @param id The Id to match.
     * @return The matching PhaseInfo, UNKNOWN if not known.
     */
    public static PhaseInfo fromId(int id) {
        return Stream
                .of(PhaseInfo.values())
                .filter(p -> p.getId() == id)
                .findFirst()
                .orElse(PhaseInfo.UNKNOWN);
    }

}