/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.time;

import java.time.Duration;
import java.time.Period;
import java.time.temporal.TemporalAmount;
import java.util.Comparator;

/**
 * Provides an implementation for the {@link Comparator} interface for {@link TemporalAmount}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-08 (13:45)
 */
public class TemporalAmountComparator implements Comparator<TemporalAmount> {

    @Override
    public int compare(TemporalAmount ta1, TemporalAmount ta2) {
        if (ta1 instanceof Duration) {
            Duration d1 = (Duration) ta1;
            if (ta2 instanceof Duration) {
                Duration d2 = (Duration) ta2;
                return d1.compareTo(d2);
            } else {
                // ta2 must be a Period which is always bigger than a Duration
                return -1;
            }
        } else {
            // ta1 must be a Period
            Period p1 = (Period) ta1;
            if (ta2 instanceof Period) {
                Period p2 = (Period) ta2;
                if (p1.equals(p2)) {
                    return 0;
                } else if (p1.minus(p2).isNegative()) {
                    return -1;
                } else {
                    return 1;
                }
            } else {
                // ta2 must be a Duration, which is always smaller than a Period
                return 1;
            }
        }
    }
}