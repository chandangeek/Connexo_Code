package com.energyict.mdc.common.rest;

import com.elster.jupiter.util.time.Interval;
import com.fasterxml.jackson.annotation.JsonProperty;

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

    public static IntervalInfo from(Interval interval) {
        IntervalInfo info = new IntervalInfo();
        info.start = interval.getStart().toEpochMilli();
        info.end = interval.getEnd().toEpochMilli();
        return info;
    }
}
