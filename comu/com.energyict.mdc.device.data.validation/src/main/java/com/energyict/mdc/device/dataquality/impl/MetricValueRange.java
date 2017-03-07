/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.dataquality.impl;

import com.elster.jupiter.util.sql.SqlBuilder;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;

interface MetricValueRange {

    void appendHavingTo(SqlBuilder sqlBuilder, String expression);

    class IgnoreRange implements MetricValueRange {
        @Override
        public void appendHavingTo(SqlBuilder sqlBuilder, String expression) {
            sqlBuilder.append(expression);
            sqlBuilder.append(" >= 0");
        }
    }

    class ExactMatch implements MetricValueRange {

        private final long match;

        ExactMatch(long match) {
            this.match = match;
        }

        long getMatch() {
            return match;
        }

        @Override
        public void appendHavingTo(SqlBuilder sqlBuilder, String expression) {
            sqlBuilder.append(expression);
            sqlBuilder.append(" =");
            sqlBuilder.addLong(this.match);
        }
    }

    class LongRange implements MetricValueRange {

        private final Range<Long> range;

        LongRange(Range<Long> range) {
            this.range = range;
        }

        Range<Long> getRange() {
            return range;
        }

        @Override
        public void appendHavingTo(SqlBuilder sqlBuilder, String expression) {
            sqlBuilder.append(expression);
            sqlBuilder.append(" >");
            if (this.range.hasLowerBound() && this.range.lowerBoundType() == BoundType.CLOSED) {
                sqlBuilder.append("=");
            }
            sqlBuilder.addLong(this.range.hasLowerBound() ? this.range.lowerEndpoint() : Integer.MIN_VALUE);
            sqlBuilder.append("and ");
            sqlBuilder.append(expression);
            sqlBuilder.append(" <");
            if (this.range.hasUpperBound() && this.range.upperBoundType() == BoundType.CLOSED) {
                sqlBuilder.append("=");
            }
            sqlBuilder.addLong(this.range.hasUpperBound() ? this.range.upperEndpoint() : Integer.MAX_VALUE);
        }
    }
}