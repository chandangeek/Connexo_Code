/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.usagepoint.data.rest.impl;

import com.google.common.collect.Range;

import java.time.Instant;

/**
 * Defines start and end date for period of value measuring.
 */
public class MeasurementPeriod {

    public Instant start;
    public Instant end;

    public MeasurementPeriod() {
    }

    public static MeasurementPeriod from(Range<Instant> range) {
        MeasurementPeriod info = new MeasurementPeriod();
        info.start = rangeStart(range);
        info.end = rangeEnd(range);
        return info;
    }

    private static Instant rangeStart(Range<Instant> range) {
        if (range.hasLowerBound()) {
            return range.lowerEndpoint();
        } else {
            return null;
        }
    }

    private static Instant rangeEnd(Range<Instant> range) {
        if (range.hasUpperBound()) {
            return range.upperEndpoint();
        } else {
            return null;
        }
    }
}
