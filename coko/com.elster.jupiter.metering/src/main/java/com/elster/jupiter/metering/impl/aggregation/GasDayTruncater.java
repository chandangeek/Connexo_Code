/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.GasDayOptions;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

/**
 * Provides an implementation for the {@link InstantTruncater} interface
 * that is suitable for gas related data that needs to
 * truncate daily levels and up to the {@link GasDayOptions}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-08-05 (11:57)
 */
class GasDayTruncater implements InstantTruncater {
    private final GasDayOptions gasDayOptions;

    GasDayTruncater(GasDayOptions gasDayOptions) {
        this.gasDayOptions = gasDayOptions;
    }

    @Override
    public Instant truncate(Instant instant, IntervalLength intervalLength, ZoneId zoneId) {
        switch (intervalLength) {
            case YEAR1: // Intentional fall-through
            case MONTH1: // Intentional fall-through
            case DAY1: {
                int hours = this.gasDayOptions.getYearStart().getHour();
                if (hours > 0) {
                    return intervalLength.truncate(instant.minus(hours, ChronoUnit.HOURS), zoneId)
                            .plus(hours, ChronoUnit.HOURS);
                } else {
                    return intervalLength.truncate(instant, zoneId);
                }
            }
            default: {
                return intervalLength.truncate(instant, zoneId);
            }
        }
    }

}