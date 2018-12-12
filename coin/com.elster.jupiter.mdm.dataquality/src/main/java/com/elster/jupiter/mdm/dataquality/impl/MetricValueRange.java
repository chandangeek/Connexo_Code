/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.dataquality.impl;

import com.elster.jupiter.util.sql.SqlBuilder;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;

interface MetricValueRange {

    MetricValueRange AT_LEAST_ONE = new LongRange(Range.greaterThan(0L));

    void appendHavingTo(SqlBuilder sqlBuilder, String expression);

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
            if (this.range.hasLowerBound()) {
                sqlBuilder.append(expression);
                sqlBuilder.append(" >");
                if (this.range.lowerBoundType() == BoundType.CLOSED) {
                    sqlBuilder.append("=");
                }
                sqlBuilder.addLong(this.range.lowerEndpoint());
                if (this.range.hasUpperBound()) {
                    sqlBuilder.append("and ");
                }
            }
            if (this.range.hasUpperBound()) {
                sqlBuilder.append(expression);
                sqlBuilder.append(" <");
                if (this.range.upperBoundType() == BoundType.CLOSED) {
                    sqlBuilder.append("=");
                }
                sqlBuilder.addLong(this.range.upperEndpoint());
            }
        }
    }
}
