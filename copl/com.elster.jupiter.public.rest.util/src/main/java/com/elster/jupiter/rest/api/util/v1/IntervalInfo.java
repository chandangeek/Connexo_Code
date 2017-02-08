/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.api.util.v1;

import com.google.common.collect.Range;

import java.time.Instant;

public class IntervalInfo {
    public Long start;
    public Long end;

    public IntervalInfo() {
    }

    public static IntervalInfo from(Range<Instant> range) {
        IntervalInfo info = new IntervalInfo();
        info.start = rangeStart(range);
        info.end = rangeEnd(range);
        return info;
    }

    private static Long rangeStart(Range<Instant> range) {
        if (range.hasLowerBound()) {
            return range.lowerEndpoint().toEpochMilli();
        } else {
            return null;
        }
    }

    private static Long rangeEnd(Range<Instant> range) {
        if (range.hasUpperBound()) {
            return range.upperEndpoint().toEpochMilli();
        } else {
            return null;
        }
    }

}
