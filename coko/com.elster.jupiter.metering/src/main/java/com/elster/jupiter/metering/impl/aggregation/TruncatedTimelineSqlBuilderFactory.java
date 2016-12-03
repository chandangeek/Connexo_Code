/*
 * Copyright (c) 2016 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.metering.GasDayOptions;
import com.elster.jupiter.metering.impl.ServerMeteringService;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.time.DayMonthTime;

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
                if (IntervalLength.YEAR1.equals(this.intervalLength)) {
                    return new YearLevelGasRelatedTruncatedTimelineSqlBuilder(this.targetReadingType, this.intervalLength, sqlBuilder, meteringService);
                } else {
                    return new GasRelatedTruncatedTimelineSqlBuilder(this.targetReadingType, this.intervalLength, sqlBuilder, meteringService);
                }
            } else {
                return new TruncatedTimelineSqlBuilderImpl(this.targetReadingType, this.intervalLength, sqlBuilder);
            }
        }
    }

    private static class TruncatedTimelineSqlBuilderImpl implements TruncatedTimelineSqlBuilder {
        final VirtualReadingType targetReadingType;
        final IntervalLength intervalLength;
        final SqlBuilder sqlBuilder;
        String sqlName;

        private TruncatedTimelineSqlBuilderImpl(VirtualReadingType targetReadingType, IntervalLength intervalLength, SqlBuilder sqlBuilder) {
            this.targetReadingType = targetReadingType;
            this.intervalLength = intervalLength;
            this.sqlBuilder = sqlBuilder;
        }

        @Override
        public void append(String sqlName) {
            this.sqlName = sqlName;
            this.logActivity();
            this.truncate();
        }

        void truncate() {
            this.sqlBuilder.append("TRUNC(");
            this.appendTruncateExpression();
            this.sqlBuilder.append(", '");
            this.sqlBuilder.append(this.intervalLength.toOracleTruncFormatModel());
            this.sqlBuilder.append("')");
        }

        void appendTruncateExpression() {
            this.sqlBuilder.append(this.sqlName);
        }

        void logActivity() {
            Loggers.SQL.debug(() -> "Truncating " + this.sqlName + " to " + this.intervalLength.toOracleTruncFormatModel());
        }
    }

    private enum IntervalOperation {
        PLUS {
            @Override
            void appendTo(SqlBuilder sqlBuilder) {
                sqlBuilder.append(" + ");
            }
        },
        MINUS {
            @Override
            void appendTo(SqlBuilder sqlBuilder) {
                sqlBuilder.append(" - ");
            }
        };

        abstract void appendTo(SqlBuilder sqlBuilder);
    }

    private enum GasStartField {
        HOUR {
            @Override
            void appendValueTo(IntervalOperation operation, GasDayOptions gasDayOptions, SqlBuilder sqlBuilder) {
                sqlBuilder.append(String.valueOf(gasDayOptions.getYearStart().getHour()));
            }
        },
        MONTH {
            @Override
            void appendValueTo(IntervalOperation operation, GasDayOptions gasDayOptions, SqlBuilder sqlBuilder) {
                if (IntervalOperation.PLUS.equals(operation)) {
                    sqlBuilder.append(String.valueOf(gasDayOptions.getYearStart().getMonthValue() - 1));
                } else {
                    sqlBuilder.append(String.valueOf(gasDayOptions.getYearStart().getMonthValue()));
                }
            }
        };

        abstract void appendValueTo(IntervalOperation operation, GasDayOptions gasDayOptions, SqlBuilder sqlBuilder);

        void appendUnitTo(SqlBuilder sqlBuilder) {
            sqlBuilder.append(this.name());
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
        void logActivity() {
            Loggers.SQL.debug(() -> "Truncating " + this.sqlName + " to " + this.intervalLength.toOracleTruncFormatModel() + " for gas commodity using gasday start " + this.gasDayOptions
                    .getYearStart()
                    .toString());
        }

        @Override
        void appendTruncateExpression() {
            super.appendTruncateExpression();
            this.appendOracleIntervalExpression(IntervalOperation.MINUS, GasStartField.HOUR);
        }

        void appendOracleIntervalExpression(IntervalOperation operation, GasStartField field) {
            operation.appendTo(this.sqlBuilder);
            this.sqlBuilder.append("INTERVAL '");
            field.appendValueTo(operation, this.gasDayOptions, this.sqlBuilder);
            this.sqlBuilder.append("' ");
            field.appendUnitTo(this.sqlBuilder);
        }

        @Override
        void truncate() {
            this.sqlBuilder.append("(");
            super.truncate();
            this.appendOracleIntervalExpression(IntervalOperation.PLUS, GasStartField.HOUR);
            this.sqlBuilder.append(")");
        }

    }

    private static class YearLevelGasRelatedTruncatedTimelineSqlBuilder extends GasRelatedTruncatedTimelineSqlBuilder {
        YearLevelGasRelatedTruncatedTimelineSqlBuilder(VirtualReadingType targetReadingType, IntervalLength intervalLength, SqlBuilder sqlBuilder, ServerMeteringService meteringService) {
            super(targetReadingType, intervalLength, sqlBuilder, meteringService);
        }

        @Override
        void appendTruncateExpression() {
            this.sqlBuilder.append("(");
            super.appendTruncateExpression();
            this.sqlBuilder.append(")");
            this.appendOracleIntervalExpression(IntervalOperation.MINUS, GasStartField.MONTH);
        }

        @Override
        void truncate() {
            this.sqlBuilder.append("(");
            super.truncate();
            this.appendOracleIntervalExpression(IntervalOperation.PLUS, GasStartField.MONTH);
            this.sqlBuilder.append(")");
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
    }

}