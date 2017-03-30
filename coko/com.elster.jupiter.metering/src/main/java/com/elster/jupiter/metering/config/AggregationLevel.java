/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.config;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Models the supported aggregation levels for the expert function:
 * <ul>
 * <li>{@link Function#SUM}</li>
 * <li>{@link Function#AVG}</li>
 * <li>{@link Function#MIN}</li>
 * <li>{@link Function#MAX}</li>
 * </ul>
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-05 (08:47)
 */
public enum AggregationLevel {
    HOUR,
    DAY,
    WEEK,
    MONTH,
    YEAR;

    /**
     * Returns the AggregationLevel whose name is equal to
     * (ignoring case considerations) the specified String.
     *
     * @param nameIgnoreCase The String
     * @return The AggregationLevel
     */
    public static Optional<AggregationLevel> from(String nameIgnoreCase) {
        return Stream.of(values())
                .filter(aggregationLevel -> nameIgnoreCase.equalsIgnoreCase(aggregationLevel.name()))
                .findFirst();
    }

}