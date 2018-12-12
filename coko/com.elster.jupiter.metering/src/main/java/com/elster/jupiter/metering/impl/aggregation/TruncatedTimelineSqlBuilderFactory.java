/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.GasDayOptions;
import com.elster.jupiter.metering.impl.ServerMeteringService;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.time.DayMonthTime;

import java.time.Instant;
import java.time.Month;
import java.time.MonthDay;
import java.util.Collections;
import java.util.List;

/**
 * Provides factory services for {@link TruncatedTimelineSqlBuilder}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-07-18 (10:55)
 */
final class TruncatedTimelineSqlBuilderFactory {

    static TruncatedTimelineSqlBuilderFrom truncate(VirtualReadingType targetReadingType) {
        return new TruncatedTimelineSqlBuilderFrom(targetReadingType);
    }

    static final class TruncatedTimelineSqlBuilderFrom {
        private final VirtualReadingType targetReadingType;

        TruncatedTimelineSqlBuilderFrom(VirtualReadingType targetReadingType) {
            this.targetReadingType = targetReadingType;
        }

        TruncatedTimelineSqlBuilderTo to(IntervalLength intervalLength) {
            return new TruncatedTimelineSqlBuilderTo(this.targetReadingType, intervalLength);
        }
    }

    static final class TruncatedTimelineSqlBuilderTo {
        private final VirtualReadingType targetReadingType;
        private final IntervalLength intervalLength;

        TruncatedTimelineSqlBuilderTo(VirtualReadingType targetReadingType, IntervalLength intervalLength) {
            this.targetReadingType = targetReadingType;
            this.intervalLength = intervalLength;
        }

        TruncatedTimelineSqlBuilder using(SqlBuilder sqlBuilder, ServerMeteringService meteringService) {
            if (this.targetReadingType.isGas()) {
                return new GasRelatedTruncatedTimelineSqlBuilder(this.targetReadingType, this.intervalLength, sqlBuilder, meteringService);
            } else {
                return new TruncatedTimelineSqlBuilderImpl(this.targetReadingType, this.intervalLength, sqlBuilder);
            }
        }
    }

    private static class TruncatedTimelineSqlBuilderImpl implements TruncatedTimelineSqlBuilder {
        final VirtualReadingType targetReadingType;
        final IntervalLength intervalLength;
        final SqlBuilder sqlBuilder;

        private TruncatedTimelineSqlBuilderImpl(VirtualReadingType targetReadingType, IntervalLength intervalLength, SqlBuilder sqlBuilder) {
            this.targetReadingType = targetReadingType;
            this.intervalLength = intervalLength;
            this.sqlBuilder = sqlBuilder;
        }

        @Override
        public void append(String sqlName) {
            this.logActivity(sqlName);
            this.truncate(sqlName);
        }

        void truncate(String sqlName) {
            this.intervalLength.appendTruncation(this.sqlBuilder, sqlName);
        }

        void logActivity(String sqlName) {
            Loggers.SQL.debug(() -> "Truncating " + sqlName + " to " + this.intervalLength);
        }
    }

    private static class GasRelatedTruncatedTimelineSqlBuilder extends TruncatedTimelineSqlBuilderImpl {
        final GasDayOptions gasDayOptions;

        GasRelatedTruncatedTimelineSqlBuilder(VirtualReadingType targetReadingType, IntervalLength intervalLength, SqlBuilder sqlBuilder, ServerMeteringService meteringService) {
            super(targetReadingType, intervalLength, sqlBuilder);
            this.gasDayOptions = findGasDayOptionsOrStubIfNotConfigured(meteringService);
        }

        static GasDayOptions findGasDayOptionsOrStubIfNotConfigured(ServerMeteringService meteringService) {
            return meteringService.getGasDayOptions().orElseGet(NoGasDayOptions::new);
        }

        @Override
        void logActivity(String sqlName) {
            Loggers.SQL.debug(() -> "Truncating " + sqlName + " to " + this.intervalLength + " for gas commodity using gasday start " + this.gasDayOptions.getYearStart().toString());
        }


        @Override
        void truncate(String sqlName) {
            this.intervalLength.appendTruncation(this.gasDayOptions, this.sqlBuilder, sqlName);
        }

    }

    private static final class NoGasDayOptions implements GasDayOptions {
        @Override
        public DayMonthTime getYearStart() {
            return DayMonthTime.fromMidnight(MonthDay.of(Month.JANUARY, 1));
        }

        @Override
        public List<RelativePeriod> getRelativePeriods() {
            return Collections.emptyList();
        }

        @Override
        public Instant addTo(Instant timestamp) {
            return timestamp;
        }

        @Override
        public Instant subtractFrom(Instant timestamp) {
            return timestamp;
        }
    }

}