/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.conditions;

import com.elster.jupiter.util.time.Interval;
import com.google.common.collect.BoundType;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

public final class Where {

    private final String field;

    private Where(String field) {
        this.field = field;
    }

    public static Where where(String field) {
        return new Where(field);
    }

    public BetweenBuilder between(Object value) {
        return new BetweenBuilder(value);
    }

    public Condition isEqualTo(Object value) {
        return Operator.EQUAL.compare(field, value);
    }

    public Condition isEqualToIgnoreCase(Object value) {
        return Operator.EQUALIGNORECASE.compare(field, value);
    }

    public Condition isEqualOrBothNull(Object value) {
        return Operator.EQUALORBOTHNULL.compare(field, value);
    }

    public Condition isGreaterThan(Object value) {
        return Operator.GREATERTHAN.compare(field, value);
    }

    public Condition isGreaterThanOrEqual(Object value) {
        return Operator.GREATERTHANOREQUAL.compare(field, value);
    }

    public Condition isNotNull() {
        return Operator.ISNOTNULL.compare(field);
    }

    public Condition isNull() {
        return Operator.ISNULL.compare(field);
    }

    public Condition isLessThan(Object value) {
        return Operator.LESSTHAN.compare(field, value);
    }

    public Condition isLessThanOrEqual(Object value) {
        return Operator.LESSTHANOREQUAL.compare(field, value);
    }

    public Condition like(String value) {
        return Operator.LIKE.compare(field, toOracleSql(value));
    }

    public Condition likeIgnoreCase(String value) {
        return Operator.LIKEIGNORECASE.compare(field, toOracleSql(value));
    }

    /**
     * Translate the string literal value into an oracle equivalent.
     * Supported wildcards are Astrix (*) and question mark(?), corresponding to the sql like operators % and _.
     * All known like operators will be escaped to avoid sql injection. Not sure that is enough for security.
     */
    public static String toOracleSql(String value) {
        // escape sql like operators
        for (String keyword: Arrays.asList("\\", "_", "%")) {
            value=value.replace(keyword,"\\"+keyword);
        }
        // transform un-escaped wildcards * and ? to sql like operators
        value=value.replaceAll("([^\\\\]|^)\\*", "$1%");
        value=value.replaceAll("([^\\\\]|^)\\?", "$1_");

        // transform escaped wildcards * and ? to their unescaped literal that does not have any meaning in SQL anyway
        // We need to search for double escape: it was doubled in the little loop on top of this method
        value=value.replaceAll("\\\\\\\\\\*", "*");
        value=value.replaceAll("\\\\\\\\\\?", "?");

        return value;
    }

    public Condition isNotEqual(Object value) {
        return Operator.NOTEQUAL.compare(field, value);
    }

    public Condition isNotEqualAndNotBothNull(Object value) {
        return Operator.NOTEQUALANDNOTBOTHNULL.compare(field, value);
    }

    public Condition matches(Object value, String options) {
        return Operator.REGEXP_LIKE.compare(field, value, options);
    }

    public Condition soundsAs(Object value) {
        return Operator.SOUNDSAS.compare(field, value);
    }

    private Condition compare(Instant date, Operator operator) {
        return date == null ? Condition.TRUE : operator.compare(field, date);
    }

    private Condition after(Instant date) {
        return compare(date, Operator.GREATERTHAN);
    }

    private Condition afterOrEqual(Instant date) {
        return compare(date, Operator.GREATERTHANOREQUAL);
    }

    private Condition before(Instant date) {
        return compare(date, Operator.LESSTHAN);
    }

    private Condition beforeOrEqual(Instant date) {
        return compare(date, Operator.LESSTHANOREQUAL);
    }

    @Deprecated
    public Condition inOpen(Interval interval) {
        return after(interval.getStart()).and(before(interval.getEnd()));
    }

    @Deprecated
    public Condition inClosed(Interval interval) {
        return afterOrEqual(interval.getStart()).and(beforeOrEqual(interval.getEnd()));
    }

    @Deprecated
    public Condition inOpenClosed(Interval interval) {
        return after(interval.getStart()).and(beforeOrEqual(interval.getEnd()));
    }

    @Deprecated
    public Condition inClosedOpen(Interval interval) {
        return afterOrEqual(interval.getStart()).and(before(interval.getEnd()));
    }

    public Condition isEffective() {
        return new Effective(field);
    }

    public Condition isEffective(Instant instant) {
        return where(field + ".start").isLessThanOrEqual(instant.toEpochMilli()).and(where(field + ".end").isGreaterThan(instant.toEpochMilli()));
    }

    public Condition isEffective(Range<Instant> range) {
    	// for isEffective we know the interval has to interpreted as closedOpen
    	Condition result = Condition.TRUE;
    	if (range.hasLowerBound()) {
    		Where end = Where.where(field + ".end");
    		long endpoint = range.lowerEndpoint().toEpochMilli();
            result = result.and(end.isGreaterThan(endpoint));
    	}
    	if (range.hasUpperBound()) {
    		boolean open = range.upperBoundType().equals(BoundType.OPEN);
    		Where start = Where.where(field + ".start");
    		long endpoint = range.upperEndpoint().toEpochMilli();
            result = result.and(open ? start.isLessThan(endpoint) : start.isLessThanOrEqual(endpoint));
    	}
    	return result;
    }

    public Condition in(List<?> values) {
        return ListOperator.IN.contains(field, values);
    }

    public Condition in(Range<?> range) {
        Condition result = Condition.TRUE;
        if (range.hasLowerBound()) {
            boolean open = range.lowerBoundType().equals(BoundType.OPEN);
            result = result.and(open ? isGreaterThan(range.lowerEndpoint()) : isGreaterThanOrEqual(range.lowerEndpoint()));
        }
        if (range.hasUpperBound()) {
            boolean open = range.upperBoundType().equals(BoundType.OPEN);
            result = result.and(open ? isLessThan(range.upperEndpoint()) : isLessThanOrEqual(range.upperEndpoint()));
        }
        return result;
    }

    @Deprecated
    @Override
    public boolean equals(Object other) {
        return super.equals(other);
    }

    public class BetweenBuilder {

        private final Object lowerValue;

        private BetweenBuilder(Object value) {
            this.lowerValue = value;
        }

        public Condition and(Object value) {
            return Operator.BETWEEN.compare(field, lowerValue, value);
        }
    }
}

