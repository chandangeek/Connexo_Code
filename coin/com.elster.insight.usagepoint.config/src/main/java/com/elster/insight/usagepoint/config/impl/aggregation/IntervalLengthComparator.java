package com.elster.insight.usagepoint.config.impl.aggregation;

import java.util.Comparator;

/**
 * Provides and implementation for the Comparator interface
 * that compares {@link IntervalLength}s on the related TemporalAmount
 * rather than on the order of the enum element.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-09 (10:54)
 */
public class IntervalLengthComparator implements Comparator<IntervalLength> {

    @Override
    public int compare(IntervalLength first, IntervalLength second) {
        return new TemporalAmountComparator().compare(first.toTemporalAmount(), second.toTemporalAmount());
    }

}