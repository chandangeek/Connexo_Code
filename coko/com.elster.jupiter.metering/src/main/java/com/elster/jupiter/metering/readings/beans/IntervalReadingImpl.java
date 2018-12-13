/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.readings.beans;

import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.readings.IntervalReading;
import com.elster.jupiter.metering.readings.ReadingQuality;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class IntervalReadingImpl extends BaseReadingImpl implements IntervalReading {

    private IntervalReadingImpl(Instant timeStamp, BigDecimal value) {
        super(timeStamp, value);
    }

    public static IntervalReadingImpl of(Instant timeStamp, BigDecimal value, List<? extends ReadingQuality> readingQualities) {
        IntervalReadingImpl intervalReading = new IntervalReadingImpl(timeStamp, value);
        if (readingQualities != null) {
            readingQualities.forEach(intervalReading::addQuality);
        }
        return intervalReading;
    }

    public static IntervalReadingImpl of(Instant timeStamp, BigDecimal value, Set<ReadingQualityType> readingQualityTypes) {
        IntervalReadingImpl intervalReading = new IntervalReadingImpl(timeStamp, value);
        if (readingQualityTypes != null) {
            readingQualityTypes.forEach(intervalReading::addQuality);
        }
        return intervalReading;
    }

    /**
     * Create an interval reading without any reading qualities.
     */
    public static IntervalReadingImpl of(Instant timeStamp, BigDecimal value) {
        return of(timeStamp, value, Collections.<ReadingQuality>emptyList());
    }
}