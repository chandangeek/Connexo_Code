/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.metering.impl;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.metering.impl.matchers.ItemMatcher;
import com.energyict.mdc.metering.impl.matchers.Matcher;
import com.energyict.mdc.metering.impl.matchers.Range;
import com.energyict.mdc.metering.impl.matchers.RangeMatcher;

enum AccumulationMapping {

    SUMMATION(Accumulation.SUMMATION,
            ItemMatcher.itemsDontMatchFor(0, 93, 94, 96, 97, 98, 99),
            ItemMatcher.itemMatcherFor(8),
            RangeMatcher.rangeMatcherFor(new Range(1, 63)),
            ItemMatcher.itemMatcherFor(
                    TimeDuration.days(1).getSeconds(),
                    TimeDuration.weeks(1).getSeconds(),
                    TimeDuration.months(1).getSeconds())),
    BLUKQUANTITY(Accumulation.BULKQUANTITY,
            ItemMatcher.itemsDontMatchFor(0, 93, 94, 96, 97, 98, 99),
            ItemMatcher.itemMatcherFor(8),
            RangeMatcher.rangeMatcherFor(new Range(0, 63)), Matcher.DONT_CARE),
    CUMULATIVE(Accumulation.CUMULATIVE,
            ItemMatcher.itemsDontMatchFor(0, 93, 94, 96, 97, 98, 99),
            ItemMatcher.itemMatcherFor(1,2,11,12,21,22),
            RangeMatcher.rangeMatcherFor(new Range(0, 63)), Matcher.DONT_CARE),
    DELTADATA(Accumulation.DELTADELTA,
            ItemMatcher.itemsDontMatchFor(0, 93, 94, 96, 97, 98, 99),
            ItemMatcher.itemMatcherFor(5,6),
            RangeMatcher.rangeMatcherFor(new Range(0, 63)), Matcher.DONT_CARE),
    INDICATING(Accumulation.INDICATING,
            ItemMatcher.itemsDontMatchFor(0, 93, 94, 96, 97, 98, 99),
            ItemMatcher.itemMatcherFor(3, 4, 7, 13, 14, 15, 16, 23, 24, 25, 26, 27, 28),
            RangeMatcher.rangeMatcherFor(new Range(0, 63)), Matcher.DONT_CARE);

    private final Accumulation accumulation;
    private final Matcher<Integer> cFieldMatcher;
    private final Matcher<Integer> dFieldMatcher;
    private final Matcher<Integer> eFieldMatcher;
    private final Matcher<Integer> timeDurationMatcher;

    AccumulationMapping(Accumulation accumulation, Matcher<Integer> cFieldMatcher, Matcher<Integer> dFieldMatcher, Matcher<Integer> eFieldMatcher, Matcher<Integer> timeDurationMatcher) {
        this.accumulation = accumulation;
        this.cFieldMatcher = cFieldMatcher;
        this.dFieldMatcher = dFieldMatcher;
        this.eFieldMatcher = eFieldMatcher;
        this.timeDurationMatcher = timeDurationMatcher;
    }

    public static Accumulation getAccumulationFor(ObisCode obisCode, TimeDuration interval) {
        if (interval == null) {
            /* Just to make sure the match can be made for Summation */
            interval = new TimeDuration(0);
        }
        if (obisCode != null && ObisCodeUtil.isElectricity(obisCode)) {
            for (AccumulationMapping accumulationMapping : values()) {
                if (accumulationMapping.cFieldMatcher.match(obisCode.getC()) &&
                        accumulationMapping.dFieldMatcher.match(obisCode.getD()) &&
                        accumulationMapping.eFieldMatcher.match(obisCode.getE()) &&
                        accumulationMapping.timeDurationMatcher.match(interval.getSeconds())) {
                    return accumulationMapping.accumulation;
                }
            }
        }
        return Accumulation.NOTAPPLICABLE;
    }

    Accumulation getAccumulation() {
        return accumulation;
    }

    Matcher<Integer> getcFieldMatcher() {
        return cFieldMatcher;
    }

    Matcher<Integer> getdFieldMatcher() {
        return dFieldMatcher;
    }

    Matcher<Integer> geteFieldMatcher() {
        return eFieldMatcher;
    }

    Matcher<Integer> getTimeDurationMatcher() {
        return timeDurationMatcher;
    }
}
