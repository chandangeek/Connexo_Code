package com.elster.insight.common.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Range;

import java.time.Instant;

/**
 * Created by bvn on 8/6/14.
 */
public class IntervalInfo {
    @JsonProperty("start")
    public Long start;
    @JsonProperty("end")
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
        }
        else {
            return null;
        }
    }

    private static Long rangeEnd(Range<Instant> range) {
        if (range.hasUpperBound()) {
            return range.upperEndpoint().toEpochMilli();
        }
        else {
            return null;
        }
    }

}
