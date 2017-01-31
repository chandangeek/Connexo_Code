/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import java.time.Instant;
import java.time.ZoneId;

/**
 * Provides an implementation for the {@link InstantTruncater} interface
 * that simply delegates to {@link IntervalLength#truncate(Instant, ZoneId)}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-08-05 (11:56)
 */
class SimpleTruncater implements InstantTruncater {
    @Override
    public Instant truncate(Instant instant, IntervalLength intervalLength, ZoneId zoneId) {
        return intervalLength.truncate(instant, zoneId);
    }
}