/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.metering.GasDayOptions;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.AggregationLevel;
import com.elster.jupiter.metering.config.PartiallySpecifiedReadingTypeRequirement;
import com.elster.jupiter.util.sql.SqlBuilder;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAmount;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Models the length of an interval as specified by the
 * {@link ReadingType#getMacroPeriod()} and {@link ReadingType#getMeasuringPeriod()}
 * of a {@link ReadingType}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-09 (09:27)
 */
public enum IntervalLength {

    MINUTE1(Duration.ofMinutes(1)) {
        @Override
        Set<IntervalLength> multiples() {
            return EnumSet.of(
                    IntervalLength.MINUTE1,
                    IntervalLength.MINUTE2,
                    IntervalLength.MINUTE3,
                    IntervalLength.MINUTE4,
                    IntervalLength.MINUTE5,
                    IntervalLength.MINUTE6,
                    IntervalLength.MINUTE10,
                    IntervalLength.MINUTE12,
                    IntervalLength.MINUTE15,
                    IntervalLength.MINUTE20,
                    IntervalLength.MINUTE30,
                    IntervalLength.HOUR1,
                    IntervalLength.HOUR2,
                    IntervalLength.HOUR3,
                    IntervalLength.HOUR4,
                    IntervalLength.HOUR6,
                    IntervalLength.HOUR12,
                    IntervalLength.DAY1,
                    IntervalLength.WEEK1,
                    IntervalLength.MONTH1,
                    IntervalLength.YEAR1);
        }

        @Override
        public BigDecimal getVolumeFlowConversionFactor() {
            return BigDecimal.valueOf(60L);
        }

        @Override
        Instant truncate(Instant instant, ZoneId zone) {
            return instant.truncatedTo(ChronoUnit.MINUTES);
        }

        @Override
        public AggregationFunction appendTruncation(SqlBuilder sqlBuilder, String expression) {
            throw new UnsupportedOperationException("Disaggregation is not supported (yet)");
        }

        @Override
        public void appendTruncation(GasDayOptions gasDayOptions, SqlBuilder sqlBuilder, String expression) {
            this.appendTruncation(sqlBuilder, expression);  // Intervals < day do not need special treatment for gas
        }
    },
    MINUTE2(Duration.ofMinutes(2)) {
        @Override
        Set<IntervalLength> multiples() {
            return EnumSet.of(
                    IntervalLength.MINUTE2,
                    IntervalLength.MINUTE4,
                    IntervalLength.MINUTE6,
                    IntervalLength.MINUTE10,
                    IntervalLength.MINUTE12,
                    IntervalLength.MINUTE20,
                    IntervalLength.MINUTE30,
                    IntervalLength.HOUR1,
                    IntervalLength.HOUR2,
                    IntervalLength.HOUR3,
                    IntervalLength.HOUR4,
                    IntervalLength.HOUR6,
                    IntervalLength.DAY1,
                    IntervalLength.WEEK1,
                    IntervalLength.MONTH1,
                    IntervalLength.YEAR1);
        }

        @Override
        public BigDecimal getVolumeFlowConversionFactor() {
            return BigDecimal.valueOf(30L);
        }

        @Override
        Instant truncate(Instant instant, ZoneId zone) {
            return this.truncateMinutes(instant, zone, 2);
        }

        @Override
        public AggregationFunction appendTruncation(SqlBuilder sqlBuilder, String expression) {
            return this.appendSpecialTruncation(sqlBuilder, expression, "120000");    // 2 minutes is 120 seconds or 120000 millis
        }

        @Override
        public void appendTruncation(GasDayOptions gasDayOptions, SqlBuilder sqlBuilder, String expression) {
            this.appendTruncation(sqlBuilder, expression);  // Intervals < day do not need special treatment for gas
        }
    },
    MINUTE3(Duration.ofMinutes(3)) {
        @Override
        Set<IntervalLength> multiples() {
            return EnumSet.of(
                    IntervalLength.MINUTE3,
                    IntervalLength.MINUTE6,
                    IntervalLength.MINUTE12,
                    IntervalLength.MINUTE15,
                    IntervalLength.MINUTE30,
                    IntervalLength.HOUR1,
                    IntervalLength.HOUR2,
                    IntervalLength.HOUR3,
                    IntervalLength.HOUR4,
                    IntervalLength.HOUR6,
                    IntervalLength.DAY1,
                    IntervalLength.WEEK1,
                    IntervalLength.MONTH1,
                    IntervalLength.YEAR1);
        }

        @Override
        public BigDecimal getVolumeFlowConversionFactor() {
            return BigDecimal.valueOf(20L);
        }

        @Override
        Instant truncate(Instant instant, ZoneId zone) {
            return this.truncateMinutes(instant, zone, 3);
        }

        @Override
        public AggregationFunction appendTruncation(SqlBuilder sqlBuilder, String expression) {
            return this.appendSpecialTruncation(sqlBuilder, expression, "180000");    // 3 minutes is 180 seconds or 180000 millis
        }

        @Override
        public void appendTruncation(GasDayOptions gasDayOptions, SqlBuilder sqlBuilder, String expression) {
            this.appendTruncation(sqlBuilder, expression);  // Intervals < day do not need special treatment for gas
        }
    },
    MINUTE4(Duration.ofMinutes(4)) {
        @Override
        Set<IntervalLength> multiples() {
            return EnumSet.of(
                    IntervalLength.MINUTE4,
                    IntervalLength.MINUTE12,
                    IntervalLength.MINUTE20,
                    IntervalLength.HOUR1,
                    IntervalLength.HOUR2,
                    IntervalLength.HOUR3,
                    IntervalLength.HOUR4,
                    IntervalLength.HOUR6,
                    IntervalLength.DAY1,
                    IntervalLength.WEEK1,
                    IntervalLength.MONTH1,
                    IntervalLength.YEAR1);
        }

        @Override
        public BigDecimal getVolumeFlowConversionFactor() {
            return BigDecimal.valueOf(15L);
        }

        @Override
        Instant truncate(Instant instant, ZoneId zone) {
            return this.truncateMinutes(instant, zone, 4);
        }

        @Override
        public AggregationFunction appendTruncation(SqlBuilder sqlBuilder, String expression) {
            return this.appendSpecialTruncation(sqlBuilder, expression, "240000");    // 4 minutes is 240 seconds or 240000 millis
        }

        @Override
        public void appendTruncation(GasDayOptions gasDayOptions, SqlBuilder sqlBuilder, String expression) {
            this.appendTruncation(sqlBuilder, expression);  // Intervals < day do not need special treatment for gas
        }
    },
    MINUTE5(Duration.ofMinutes(5)) {
        @Override
        Set<IntervalLength> multiples() {
            return EnumSet.of(
                    IntervalLength.MINUTE5,
                    IntervalLength.MINUTE10,
                    IntervalLength.MINUTE15,
                    IntervalLength.MINUTE20,
                    IntervalLength.MINUTE30,
                    IntervalLength.HOUR1,
                    IntervalLength.HOUR2,
                    IntervalLength.HOUR3,
                    IntervalLength.HOUR4,
                    IntervalLength.HOUR6,
                    IntervalLength.DAY1,
                    IntervalLength.WEEK1,
                    IntervalLength.MONTH1,
                    IntervalLength.YEAR1);
        }

        @Override
        public BigDecimal getVolumeFlowConversionFactor() {
            return BigDecimal.valueOf(12L);
        }

        @Override
        Instant truncate(Instant instant, ZoneId zone) {
            return this.truncateMinutes(instant, zone, 5);
        }

        @Override
        public AggregationFunction appendTruncation(SqlBuilder sqlBuilder, String expression) {
            return this.appendSpecialTruncation(sqlBuilder, expression, "3000000");    // 5 minutes is 300 seconds or 300000 millis
        }

        @Override
        public void appendTruncation(GasDayOptions gasDayOptions, SqlBuilder sqlBuilder, String expression) {
            this.appendTruncation(sqlBuilder, expression);  // Intervals < day do not need special treatment for gas
        }
    },
    MINUTE6(Duration.ofMinutes(6)) {
        @Override
        Set<IntervalLength> multiples() {
            return EnumSet.of(
                    IntervalLength.MINUTE6,
                    IntervalLength.MINUTE12,
                    IntervalLength.MINUTE30,
                    IntervalLength.HOUR1,
                    IntervalLength.HOUR2,
                    IntervalLength.HOUR3,
                    IntervalLength.HOUR4,
                    IntervalLength.HOUR6,
                    IntervalLength.DAY1,
                    IntervalLength.WEEK1,
                    IntervalLength.MONTH1,
                    IntervalLength.YEAR1);
        }

        @Override
        public BigDecimal getVolumeFlowConversionFactor() {
            return BigDecimal.valueOf(10L);
        }

        @Override
        Instant truncate(Instant instant, ZoneId zone) {
            return this.truncateMinutes(instant, zone, 6);
        }

        @Override
        public AggregationFunction appendTruncation(SqlBuilder sqlBuilder, String expression) {
            return this.appendSpecialTruncation(sqlBuilder, expression, "3600000");    // 6 minutes is 360 seconds or 360000 millis
        }

        @Override
        public void appendTruncation(GasDayOptions gasDayOptions, SqlBuilder sqlBuilder, String expression) {
            this.appendTruncation(sqlBuilder, expression);  // Intervals < day do not need special treatment for gas
        }
    },
    MINUTE10(Duration.ofMinutes(10)) {
        @Override
        Set<IntervalLength> multiples() {
            return EnumSet.of(
                    IntervalLength.MINUTE10,
                    IntervalLength.MINUTE20,
                    IntervalLength.MINUTE30,
                    IntervalLength.HOUR1,
                    IntervalLength.HOUR2,
                    IntervalLength.HOUR3,
                    IntervalLength.HOUR4,
                    IntervalLength.HOUR6,
                    IntervalLength.DAY1,
                    IntervalLength.WEEK1,
                    IntervalLength.MONTH1,
                    IntervalLength.YEAR1);
        }

        @Override
        public BigDecimal getVolumeFlowConversionFactor() {
            return BigDecimal.valueOf(6L);
        }

        @Override
        Instant truncate(Instant instant, ZoneId zone) {
            return this.truncateMinutes(instant, zone, 10);
        }

        @Override
        public AggregationFunction appendTruncation(SqlBuilder sqlBuilder, String expression) {
            return this.appendSpecialTruncation(sqlBuilder, expression, "600000");    // 10 minutes is 600 seconds or 600000 millis
        }

        @Override
        public void appendTruncation(GasDayOptions gasDayOptions, SqlBuilder sqlBuilder, String expression) {
            this.appendTruncation(sqlBuilder, expression);  // Intervals < day do not need special treatment for gas
        }
    },
    MINUTE12(Duration.ofMinutes(12)) {
        @Override
        Set<IntervalLength> multiples() {
            return EnumSet.of(
                    IntervalLength.MINUTE12,
                    IntervalLength.HOUR1,
                    IntervalLength.HOUR2,
                    IntervalLength.HOUR3,
                    IntervalLength.HOUR4,
                    IntervalLength.HOUR6,
                    IntervalLength.DAY1,
                    IntervalLength.WEEK1,
                    IntervalLength.MONTH1,
                    IntervalLength.YEAR1);
        }

        @Override
        public BigDecimal getVolumeFlowConversionFactor() {
            return BigDecimal.valueOf(5L);
        }

        @Override
        Instant truncate(Instant instant, ZoneId zone) {
            return this.truncateMinutes(instant, zone, 12);
        }

        @Override
        public AggregationFunction appendTruncation(SqlBuilder sqlBuilder, String expression) {
            return this.appendSpecialTruncation(sqlBuilder, expression, "720000");    // 12 minutes is 720 seconds or 720000 millis
        }

        @Override
        public void appendTruncation(GasDayOptions gasDayOptions, SqlBuilder sqlBuilder, String expression) {
            this.appendTruncation(sqlBuilder, expression);  // Intervals < day do not need special treatment for gas
        }
    },
    MINUTE15(Duration.ofMinutes(15)) {
        @Override
        Set<IntervalLength> multiples() {
            return EnumSet.of(
                    IntervalLength.MINUTE15,
                    IntervalLength.MINUTE30,
                    IntervalLength.HOUR1,
                    IntervalLength.HOUR2,
                    IntervalLength.HOUR3,
                    IntervalLength.HOUR4,
                    IntervalLength.HOUR6,
                    IntervalLength.DAY1,
                    IntervalLength.WEEK1,
                    IntervalLength.MONTH1,
                    IntervalLength.YEAR1);
        }

        @Override
        public BigDecimal getVolumeFlowConversionFactor() {
            return BigDecimal.valueOf(4L);
        }

        @Override
        Instant truncate(Instant instant, ZoneId zone) {
            return this.truncateMinutes(instant, zone, 15);
        }

        @Override
        public AggregationFunction appendTruncation(SqlBuilder sqlBuilder, String expression) {
            return this.appendSpecialTruncation(sqlBuilder, expression, "900000");    // 15 minutes is 900 seconds or 900000 millis
        }

        @Override
        public void appendTruncation(GasDayOptions gasDayOptions, SqlBuilder sqlBuilder, String expression) {
            this.appendTruncation(sqlBuilder, expression);  // Intervals < day do not need special treatment for gas
        }
    },
    MINUTE20(Duration.ofMinutes(20)) {
        @Override
        Set<IntervalLength> multiples() {
            return EnumSet.of(
                    IntervalLength.MINUTE20,
                    IntervalLength.HOUR1,
                    IntervalLength.HOUR2,
                    IntervalLength.HOUR3,
                    IntervalLength.HOUR4,
                    IntervalLength.HOUR6,
                    IntervalLength.DAY1,
                    IntervalLength.WEEK1,
                    IntervalLength.MONTH1,
                    IntervalLength.YEAR1);
        }

        @Override
        public BigDecimal getVolumeFlowConversionFactor() {
            return BigDecimal.valueOf(3L);
        }

        @Override
        Instant truncate(Instant instant, ZoneId zone) {
            return this.truncateMinutes(instant, zone, 20);
        }

        @Override
        public AggregationFunction appendTruncation(SqlBuilder sqlBuilder, String expression) {
            return this.appendSpecialTruncation(sqlBuilder, expression, "1200000");    // 20 minutes is 1200 seconds or 1200000 millis
        }

        @Override
        public void appendTruncation(GasDayOptions gasDayOptions, SqlBuilder sqlBuilder, String expression) {
            this.appendTruncation(sqlBuilder, expression);  // Intervals < day do not need special treatment for gas
        }
    },
    MINUTE30(Duration.ofMinutes(30)) {
        @Override
        Set<IntervalLength> multiples() {
            return EnumSet.of(
                    IntervalLength.MINUTE30,
                    IntervalLength.HOUR1,
                    IntervalLength.HOUR2,
                    IntervalLength.HOUR3,
                    IntervalLength.HOUR4,
                    IntervalLength.HOUR6,
                    IntervalLength.DAY1,
                    IntervalLength.WEEK1,
                    IntervalLength.MONTH1,
                    IntervalLength.YEAR1);
        }

        @Override
        public BigDecimal getVolumeFlowConversionFactor() {
            return BigDecimal.valueOf(2L);
        }

        @Override
        Instant truncate(Instant instant, ZoneId zone) {
            return this.truncateMinutes(instant, zone, 30);
        }

        @Override
        public AggregationFunction appendTruncation(SqlBuilder sqlBuilder, String expression) {
            return this.appendSpecialTruncation(sqlBuilder, expression, "1800000");    // 30 minutes is 1800 seconds or 1800000 millis
        }

        @Override
        public void appendTruncation(GasDayOptions gasDayOptions, SqlBuilder sqlBuilder, String expression) {
            this.appendTruncation(sqlBuilder, expression);  // Intervals < day do not need special treatment for gas
        }
    },
    HOUR1(Duration.ofHours(1)) {
        @Override
        Set<IntervalLength> multiples() {
            return EnumSet.of(
                    IntervalLength.HOUR1,
                    IntervalLength.HOUR2,
                    IntervalLength.HOUR3,
                    IntervalLength.HOUR4,
                    IntervalLength.HOUR6,
                    IntervalLength.DAY1,
                    IntervalLength.WEEK1,
                    IntervalLength.MONTH1,
                    IntervalLength.YEAR1);
        }

        @Override
        public AggregationFunction appendTruncation(SqlBuilder sqlBuilder, String expression) {
            return this.appendSimpleOracleTruncation(sqlBuilder, expression, TruncFormatModels.HOUR);
        }


        @Override
        public void appendTruncation(GasDayOptions gasDayOptions, SqlBuilder sqlBuilder, String expression) {
            this.appendTruncation(sqlBuilder, expression);  // Intervals < day do not need special treatment for gas
        }
        @Override
        public BigDecimal getVolumeFlowConversionFactor() {
            return BigDecimal.ONE;
        }

        @Override
        Instant truncate(Instant instant, ZoneId zone) {
            return instant.truncatedTo(ChronoUnit.HOURS);
        }
    },
    HOUR2(Duration.ofHours(2)) {
        @Override
        Set<IntervalLength> multiples() {
            return EnumSet.of(
                    IntervalLength.HOUR2,
                    IntervalLength.HOUR4,
                    IntervalLength.HOUR6,
                    IntervalLength.DAY1,
                    IntervalLength.WEEK1,
                    IntervalLength.MONTH1,
                    IntervalLength.YEAR1);
        }

        @Override
        public BigDecimal getVolumeFlowConversionFactor() {
            return BigDecimal.ONE.divide(BigDecimal.valueOf(2L), 1, RoundingMode.UNNECESSARY);
        }

        @Override
        Instant truncate(Instant instant, ZoneId zone) {
            return this.truncateHours(instant, zone, 2);
        }

        @Override
        public AggregationFunction appendTruncation(SqlBuilder sqlBuilder, String expression) {
            return this.appendSpecialTruncation(sqlBuilder, expression, "7200000");    // 2 hours is 7200 seconds or 7200000 millis
        }

        @Override
        public void appendTruncation(GasDayOptions gasDayOptions, SqlBuilder sqlBuilder, String expression) {
            this.appendTruncation(sqlBuilder, expression);  // Intervals < day do not need special treatment for gas
        }
    },
    HOUR3(Duration.ofHours(3)) {
        @Override
        Set<IntervalLength> multiples() {
            return EnumSet.of(
                    IntervalLength.HOUR3,
                    IntervalLength.HOUR6,
                    IntervalLength.DAY1,
                    IntervalLength.WEEK1,
                    IntervalLength.MONTH1,
                    IntervalLength.YEAR1);
        }

        @Override
        public BigDecimal getVolumeFlowConversionFactor() {
            return BigDecimal.ONE.divide(BigDecimal.valueOf(3L), 20, RoundingMode.DOWN);
        }

        @Override
        Instant truncate(Instant instant, ZoneId zone) {
            return this.truncateHours(instant, zone, 3);
        }

        @Override
        public AggregationFunction appendTruncation(SqlBuilder sqlBuilder, String expression) {
            return this.appendSpecialTruncation(sqlBuilder, expression, "10800000");    // 3 hours is 10800 seconds or 10800000 millis
        }

        @Override
        public void appendTruncation(GasDayOptions gasDayOptions, SqlBuilder sqlBuilder, String expression) {
            this.appendTruncation(sqlBuilder, expression);  // Intervals < day do not need special treatment for gas
        }
    },
    HOUR4(Duration.ofHours(4)) {
        @Override
        Set<IntervalLength> multiples() {
            return EnumSet.of(
                    IntervalLength.HOUR4,
                    IntervalLength.DAY1,
                    IntervalLength.WEEK1,
                    IntervalLength.MONTH1,
                    IntervalLength.YEAR1);
        }

        @Override
        public BigDecimal getVolumeFlowConversionFactor() {
            return BigDecimal.ONE.divide(BigDecimal.valueOf(4L), 2, RoundingMode.UNNECESSARY);
        }

        @Override
        Instant truncate(Instant instant, ZoneId zone) {
            return this.truncateHours(instant, zone, 4);
        }

        @Override
        public AggregationFunction appendTruncation(SqlBuilder sqlBuilder, String expression) {
            return this.appendSpecialTruncation(sqlBuilder, expression, "14400000");    // 4 hours is 14400 seconds or 14400000 millis
        }

        @Override
        public void appendTruncation(GasDayOptions gasDayOptions, SqlBuilder sqlBuilder, String expression) {
            this.appendTruncation(sqlBuilder, expression);  // Intervals < day do not need special treatment for gas
        }
    },
    HOUR6(Duration.ofHours(6)) {
        @Override
        Set<IntervalLength> multiples() {
            return EnumSet.of(
                    IntervalLength.HOUR6,
                    IntervalLength.DAY1,
                    IntervalLength.WEEK1,
                    IntervalLength.MONTH1,
                    IntervalLength.YEAR1);
        }

        @Override
        public BigDecimal getVolumeFlowConversionFactor() {
            return BigDecimal.ONE.divide(BigDecimal.valueOf(6L), 20, RoundingMode.DOWN);
        }

        @Override
        Instant truncate(Instant instant, ZoneId zone) {
            return this.truncateHours(instant, zone, 6);
        }

        @Override
        public AggregationFunction appendTruncation(SqlBuilder sqlBuilder, String expression) {
            return this.appendSpecialTruncation(sqlBuilder, expression, "21600000");    // 6 hours is 21600 seconds or 21600000 millis
        }

        @Override
        public void appendTruncation(GasDayOptions gasDayOptions, SqlBuilder sqlBuilder, String expression) {
            this.appendTruncation(sqlBuilder, expression);  // Intervals < day do not need special treatment for gas
        }
    },
    HOUR12(Duration.ofHours(12)) {
        @Override
        Set<IntervalLength> multiples() {
            return EnumSet.of(
                    IntervalLength.HOUR12,
                    IntervalLength.DAY1,
                    IntervalLength.WEEK1,
                    IntervalLength.MONTH1,
                    IntervalLength.YEAR1);
        }

        @Override
        public BigDecimal getVolumeFlowConversionFactor() {
            return BigDecimal.ONE.divide(BigDecimal.valueOf(12L), 20, RoundingMode.DOWN);
        }

        @Override
        Instant truncate(Instant instant, ZoneId zone) {
            return this.truncateHours(instant, zone, 12);
        }

        @Override
        public AggregationFunction appendTruncation(SqlBuilder sqlBuilder, String expression) {
            return this.appendSpecialTruncation(sqlBuilder, expression, "43200000");    // 12 hours is 43200 seconds or 43200000 millis
        }

        @Override
        public void appendTruncation(GasDayOptions gasDayOptions, SqlBuilder sqlBuilder, String expression) {
            this.appendTruncation(sqlBuilder, expression);  // Intervals < day do not need special treatment for gas
        }
    },
    DAY1(Period.ofDays(1)) {
        @Override
        Set<IntervalLength> multiples() {
            return EnumSet.of(
                    IntervalLength.DAY1,
                    IntervalLength.WEEK1,
                    IntervalLength.MONTH1,
                    IntervalLength.YEAR1);
        }

        @Override
        public AggregationFunction appendTruncation(SqlBuilder sqlBuilder, String expression) {
            return this.appendSimpleOracleTruncation(sqlBuilder, expression, TruncFormatModels.DAY);
        }

        @Override
        public void appendTruncation(GasDayOptions gasDayOptions, SqlBuilder sqlBuilder, String expression) {
            this.appendSimpleOracleTruncation(gasDayOptions, sqlBuilder, expression, TruncFormatModels.DAY);
        }

        @Override
        Instant truncate(Instant instant, ZoneId zone) {
            return instant.atZone(zone).truncatedTo(ChronoUnit.DAYS).toInstant();
        }
    },
    WEEK1(Period.ofWeeks(1)) {
        @Override
        Set<IntervalLength> multiples() {
            return EnumSet.of(
                    IntervalLength.WEEK1,
                    IntervalLength.MONTH1,
                    IntervalLength.YEAR1);
        }

        @Override
        public AggregationFunction appendTruncation(SqlBuilder sqlBuilder, String expression) {
            return this.appendSimpleOracleTruncation(sqlBuilder, expression, TruncFormatModels.WEEK);
        }

        @Override
        public void appendTruncation(GasDayOptions gasDayOptions, SqlBuilder sqlBuilder, String expression) {
            this.appendSimpleOracleTruncation(gasDayOptions, sqlBuilder, expression, TruncFormatModels.WEEK);
        }

        @Override
        Instant truncate(Instant instant, ZoneId zone) {
            ZonedDateTime zonedDateTime = instant.atZone(zone);
            int week = zonedDateTime.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
            ZonedDateTime attempt = zonedDateTime.truncatedTo(ChronoUnit.DAYS);
            ZonedDateTime candidate;
            do {
                candidate = attempt;
                attempt = candidate.minusDays(1);
            } while (attempt.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR) == week);
            return candidate.toInstant();
        }
    },
    MONTH1(Period.ofMonths(1)) {
        @Override
        Set<IntervalLength> multiples() {
            return EnumSet.of(
                    IntervalLength.MONTH1,
                    IntervalLength.YEAR1);
        }

        @Override
        public AggregationFunction appendTruncation(SqlBuilder sqlBuilder, String expression) {
            return this.appendSimpleOracleTruncation(sqlBuilder, expression, TruncFormatModels.MONTH);
        }

        @Override
        public void appendTruncation(GasDayOptions gasDayOptions, SqlBuilder sqlBuilder, String expression) {
            this.appendSimpleOracleTruncation(gasDayOptions, sqlBuilder, expression, TruncFormatModels.MONTH);
        }

        @Override
        Instant truncate(Instant instant, ZoneId zone) {
            return instant.atZone(zone).truncatedTo(ChronoUnit.DAYS).withDayOfMonth(1).toInstant();
        }

        @Override
        Instant subtractFrom(Instant instant, ZoneId zone) {
            ZonedDateTime zonedDateTime = instant.atZone(zone);
            return zonedDateTime.minus(1, ChronoUnit.MONTHS).toInstant();
        }

        @Override
        Instant addTo(Instant instant, ZoneId zone) {
            ZonedDateTime zonedDateTime = instant.atZone(zone);
            return zonedDateTime.plus(1, ChronoUnit.MONTHS).toInstant();
        }
    },
    YEAR1(Period.ofYears(1)) {
        @Override
        Set<IntervalLength> multiples() {
            return EnumSet.of(IntervalLength.YEAR1);
        }

        @Override
        public AggregationFunction appendTruncation(SqlBuilder sqlBuilder, String expression) {
            return this.appendSimpleOracleTruncation(sqlBuilder, expression, TruncFormatModels.YEAR);
        }

        @Override
        public void appendTruncation(GasDayOptions gasDayOptions, SqlBuilder sqlBuilder, String expression) {
            sqlBuilder.append("((TRUNC((");
            sqlBuilder.append(expression);
            sqlBuilder.append(".");
            sqlBuilder.append(SqlConstants.TimeSeriesColumnNames.LOCALDATE.sqlName());
            this.appendOracleIntervalExpression(MathematicalOperation.MINUS, GasStartField.HOUR, sqlBuilder, gasDayOptions);
            sqlBuilder.append(")");
            this.appendOracleIntervalExpression(MathematicalOperation.MINUS, GasStartField.MONTH, sqlBuilder, gasDayOptions);
            sqlBuilder.append(", '");
            sqlBuilder.append(TruncFormatModels.YEAR);
            sqlBuilder.append("')");
            this.appendOracleIntervalExpression(MathematicalOperation.PLUS, GasStartField.HOUR, sqlBuilder, gasDayOptions);
            sqlBuilder.append(")");
            this.appendOracleIntervalExpression(MathematicalOperation.PLUS, GasStartField.MONTH, sqlBuilder, gasDayOptions);
            sqlBuilder.append(")");
        }

        @Override
        Instant truncate(Instant instant, ZoneId zone) {
            return instant.atZone(zone).truncatedTo(ChronoUnit.DAYS).withDayOfYear(1).toInstant();
        }

        @Override
        Instant subtractFrom(Instant instant, ZoneId zone) {
            ZonedDateTime zonedDateTime = instant.atZone(zone);
            return zonedDateTime.withYear(zonedDateTime.getYear() - 1).toInstant();
        }

        @Override
        Instant addTo(Instant instant, ZoneId zone) {
            ZonedDateTime zonedDateTime = instant.atZone(zone);
            return zonedDateTime.withYear(zonedDateTime.getYear() + 1).toInstant();
        }
    },
    NOT_SUPPORTED(Duration.ofMillis(0)) {
        @Override
        protected Set<IntervalLength> multiples() {
            return EnumSet.noneOf(IntervalLength.class);
        }

        @Override
        Instant truncate(Instant instant, ZoneId zone) {
            return instant;
        }

        @Override
        public AggregationFunction appendTruncation(SqlBuilder sqlBuilder, String expression) {
            throw new UnsupportedOperationException("Unsupported interval length does not support oracle date time truncation");
        }

        @Override
        public void appendTruncation(GasDayOptions gasDayOptions, SqlBuilder sqlBuilder, String expression) {
            throw new UnsupportedOperationException("Unsupported interval length does not support oracle date time truncation");
        }
    };

    private final TemporalAmount temporalAmount;

    IntervalLength(TemporalAmount temporalAmount) {
        this.temporalAmount = temporalAmount;
    }

    TemporalAmount toTemporalAmount() {
        return temporalAmount;
    }

    /**
     * Subtracts the amount of time represented by this IntervalLength
     * from the specified Instant.
     * In other words, is equivalent to calling Instant.minus(toTemporalAmount());
     *
     * @param instant The Instant from which time will be subtracted
     * @param zone The ZoneId
     * @return The result of subtraction
     */
    Instant subtractFrom(Instant instant, ZoneId zone) {
        return instant.minus(this.toTemporalAmount());
    }

    /**
     * Adds the amount of time represented by this IntervalLength
     * from the specified Instant.
     * In other words, is equivalent to calling Instant.plus(toTemporalAmount());
     *
     * @param instant The Instant from which time will be subtracted
     * @param zone The ZoneId
     * @return The result of subtraction
     */
    Instant addTo(Instant instant, ZoneId zone) {
        return instant.plus(this.toTemporalAmount());
    }

    /**
     * Tests if this IntervalLength is a multiple of the other.
     * In other words, is there an n for which the following holds
     * this = n * other;
     * <br>
     * Is actually the reverse for {@link #multipliesTo(IntervalLength)}
     *
     * @param other The other IntervalLength
     * @return A flag that indicates if this IntervalLength is a multiple of the other
     * @see #multipliesTo(IntervalLength)
     */
    boolean isMultipleOf(IntervalLength other) {
        return other.multipliesTo(this);
    }

    /**
     * Tests if this IntervalLength can be multiplied to the other.
     * In other words, is there an n for which the following holds
     * other = n * this;
     * <br>
     * Is actually the reverse for {@link #isMultipleOf(IntervalLength)}
     *
     * @param other The other IntervalLength
     * @return A flag that indicates if this IntervalLength is a multiple of the other
     * @see #isMultipleOf(IntervalLength)
     */
    boolean multipliesTo(IntervalLength other) {
        return this.multiples().contains(other);
    }

    /**
     * Returns all the multiples of this IntervalLength,
     * i.e. all the IntervalLength to which this one
     * can be multiplied with the trunc aggregation function.
     * This is not necessarily the same as what you would
     * expect from mathematical multiplication.
     * As an example: 15min values can be multiplied to
     * 30min values but the trunc function can only
     * trunc the timestamp of one such 15min value
     * to an hour. So in fact all 15 min values within
     * the same hour are mapped to the same timestamp
     * by the trunc function and therefore they are all
     * grouped together for aggregation purposes.
     *
     * @return The multiples
     */
    abstract Set<IntervalLength> multiples();

    /**
     * Returns a copy of the the specified Instant truncated to this IntervalLength.
     * Truncation returns the biggest value that is &le; the instant
     * that is a multiple of this IntervalLength.
     * In other words:
     * <pre><code>
     *     intervalLength.truncate(timestamp).plus(intervalLength.toTemporalAmount()) &ge; instant
     * </code></pre>
     *
     * @param instant The Instant wich remain unaffected by this call
     * @param zone The ZoneId
     * @return The copy of the Instant truncated to this IntervalLength
     */
    abstract Instant truncate(Instant instant, ZoneId zone);

    protected Instant truncateMinutes(Instant instant, ZoneId zone, int multiples) {
        ZonedDateTime truncateCandidate = instant.atZone(zone).truncatedTo(ChronoUnit.MINUTES);
        int minutes = truncateCandidate.getMinute();
        if (minutes % multiples == 0) {
            return truncateCandidate.toInstant();
        } else {
            return truncateCandidate.withMinute((minutes / multiples) * multiples).toInstant();
        }
    }

    protected Instant truncateHours(Instant instant, ZoneId zone, int multiples) {
        ZonedDateTime candidate = instant.atZone(zone).truncatedTo(ChronoUnit.HOURS);
        int hours = candidate.getHour();
        if (hours % multiples == 0) {
            return candidate.toInstant();
        } else {
            return candidate.withHour((hours / multiples) * multiples).toInstant();
        }
    }

    public abstract AggregationFunction appendTruncation(SqlBuilder sqlBuilder, String expression);

    public abstract void appendTruncation(GasDayOptions gasDayOptions, SqlBuilder sqlBuilder, String expression);

    protected AggregationFunction appendSimpleOracleTruncation(SqlBuilder sqlBuilder, String expression, String oracleTruncFormat) {
        sqlBuilder.append("TRUNC(");
        sqlBuilder.append(expression);
        sqlBuilder.append(".");
        sqlBuilder.append(SqlConstants.TimeSeriesColumnNames.LOCALDATE.sqlName());
        sqlBuilder.append(", '");
        sqlBuilder.append(oracleTruncFormat);
        sqlBuilder.append("')");
        return AggregationFunction.TRUNC;
    }

    protected void appendSimpleOracleTruncation(GasDayOptions gasDayOptions, SqlBuilder sqlBuilder, String expression, String oracleTruncFormat) {
        sqlBuilder.append("(TRUNC(");
        sqlBuilder.append(expression);
        sqlBuilder.append(".");
        sqlBuilder.append(SqlConstants.TimeSeriesColumnNames.LOCALDATE.sqlName());
        this.appendOracleIntervalExpression(MathematicalOperation.MINUS, GasStartField.HOUR, sqlBuilder, gasDayOptions);
        sqlBuilder.append(", '");
        sqlBuilder.append(oracleTruncFormat);
        sqlBuilder.append("')");
        this.appendOracleIntervalExpression(MathematicalOperation.PLUS, GasStartField.HOUR, sqlBuilder, gasDayOptions);
        sqlBuilder.append(")");
    }

    void appendOracleIntervalExpression(MathematicalOperation operation, GasStartField field, SqlBuilder sqlBuilder, GasDayOptions gasDayOptions) {
        operation.appendTo(sqlBuilder);
        sqlBuilder.append("INTERVAL '");
        field.appendValueTo(operation, gasDayOptions, sqlBuilder);
        sqlBuilder.append("' ");
        field.appendUnitTo(sqlBuilder);
    }

    protected AggregationFunction appendSpecialTruncation(SqlBuilder sqlBuilder, String expression, String truncationMillis) {
        sqlBuilder.append("FLOOR(");
        sqlBuilder.append(expression);
        sqlBuilder.append(".");
        sqlBuilder.append(SqlConstants.TimeSeriesColumnNames.TIMESTAMP.sqlName());
        sqlBuilder.append("/");
        sqlBuilder.append(truncationMillis);
        sqlBuilder.append(")");
        return AggregationFunction.FLOOR;
    }

    public BigDecimal getVolumeFlowConversionFactor() {
        throw new UnsupportedOperationException("Volume to flow conversion is not (yet) supported for " + this.name());
    }

    public long getSeconds() {
        return this.temporalAmount.get(ChronoUnit.SECONDS);
    }

    public Stream<Instant> toTimeSeries(Range<Instant> period, ZoneId zoneId) {
        if (!period.hasLowerBound()) {
            throw new IllegalArgumentException("Cannot generate timeseries when start is not known");
        }
        if (!period.hasUpperBound()) {
            throw new IllegalArgumentException("Cannot generate timeseries when end is not known");
        }
        Stream.Builder<Instant> builder = Stream.builder();
        Instant current = this.truncateToFirstIntervalInPeriod(period, zoneId);
        if (period.lowerBoundType().equals(BoundType.OPEN)) {
            current = this.addTo(current, zoneId);
        }
        while (period.contains(current)) {
            builder.add(current);
            current = this.addTo(current, zoneId);
        }
        return builder.build();
    }

    private Instant truncateToFirstIntervalInPeriod(Range<Instant> period, ZoneId zoneId) {
        Instant attempt = this.truncate(period.lowerEndpoint(), zoneId);
        if (attempt.equals(period.lowerEndpoint())) {
            // Period is aligned with this IntervalLength
            return period.lowerEndpoint();
        } else {
            // Add this IntervalLength to the attempt until it is after the period start and possibly contained by the period but could also be after the period
            do {
                attempt = this.addTo(attempt, zoneId);
            } while (attempt.isBefore(period.lowerEndpoint()));
            return attempt;
        }
    }

    public static IntervalLength from(PartiallySpecifiedReadingTypeRequirement readingType) {
        MacroPeriod macroPeriod = readingType.getMacroPeriod();
        TimeAttribute measuringPeriod = readingType.getMeasuringPeriod();
        if ((macroPeriod.equals(MacroPeriod.NOTAPPLICABLE)) && (measuringPeriod.equals(TimeAttribute.NOTAPPLICABLE))) {
            return IntervalLength.NOT_SUPPORTED;
        }
        return from(macroPeriod, measuringPeriod);
    }

    public static IntervalLength from(MacroPeriod macroPeriod, TimeAttribute timeAttribute) {
        switch (macroPeriod) {
            case NOTAPPLICABLE: {
                return fromMeasurementPeriod(timeAttribute);
            }
            case DAILY: {
                return DAY1;
            }
            case MONTHLY: {
                return MONTH1;
            }
            case YEARLY: {
                return YEAR1;
            }
            case WEEKLYS: {
                return WEEK1;
            }
            case BILLINGPERIOD: // Intentional fall-through
            case SEASONAL: // Intentional fall-through
            case SPECIFIEDPERIOD: {
                return IntervalLength.NOT_SUPPORTED;
            }
            default: {
                throw new IllegalArgumentException("Unknown or unsupported macro period: " + macroPeriod.name());
            }
        }
    }

    public static IntervalLength from(AggregationLevel aggregationLevel) {
        switch (aggregationLevel) {
            case HOUR: {
                return IntervalLength.HOUR1;
            }
            case DAY: {
                return IntervalLength.DAY1;
            }
            case WEEK: {
                return IntervalLength.WEEK1;
            }
            case MONTH: {
                return IntervalLength.MONTH1;
            }
            case YEAR: {
                return IntervalLength.YEAR1;
            }
            default: {
                throw new IllegalArgumentException("Unknown or unsupported aggregation level: " + aggregationLevel.name());
            }
        }
    }

    public static IntervalLength from(ReadingType readingType) {
        return from(readingType.getMacroPeriod(), readingType.getMeasuringPeriod());
    }

    public static IntervalLength from(Duration duration) {
        EnumSet<IntervalLength> allExceptNotSupported = EnumSet.allOf(IntervalLength.class);
        allExceptNotSupported.remove(NOT_SUPPORTED);
        return allExceptNotSupported
                .stream()
                .filter(each -> each.toTemporalAmount().equals(duration))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported duration: " + duration.toString()));
    }

    public static Set<IntervalLength> multiples(IntervalLength from, IntervalLength to) {
        if (from.equals(to)) {
            return Collections.singleton(from);
        } else if (from.compareTo(to) < 0) {
            // from < to
            return from
                    .multiples()
                    .stream()
                    .filter(multiple -> multiple.compareTo(to) <= 0)
                    .collect(Collectors.toSet());
        } else {
            // to < from: be leniant and reverse the operands
            return multiples(to, from);
        }
    }

    private static IntervalLength fromMeasurementPeriod(TimeAttribute measurementPeriod) {
        switch (measurementPeriod) {
            case NOTAPPLICABLE: {
                return IntervalLength.NOT_SUPPORTED;
            }
            case MINUTE1: {
                return MINUTE1;
            }
            case MINUTE2: {
                return MINUTE2;
            }
            case MINUTE3: {
                return MINUTE3;
            }
            case MINUTE5: {
                return MINUTE5;
            }
            case MINUTE10: {
                return MINUTE10;
            }
            case MINUTE12: {
                return MINUTE12;
            }
            case MINUTE15: {
                return MINUTE15;
            }
            case MINUTE20: {
                return MINUTE20;
            }
            case MINUTE30: {
                return MINUTE30;
            }
            case MINUTE60: {
                return HOUR1;
            }
            case HOUR24: {
                return DAY1;
            }
            case FIXEDBLOCK1MIN: {
                return MINUTE1;
            }
            case FIXEDBLOCK5MIN: {
                return MINUTE5;
            }
            case FIXEDBLOCK10MIN: {
                return MINUTE10;
            }
            case FIXEDBLOCK15MIN: {
                return MINUTE15;
            }
            case FIXEDBLOCK20MIN: {
                return MINUTE20;
            }
            case FIXEDBLOCK30MIN: {
                return MINUTE30;
            }
            case FIXEDBLOCK60MIN: {
                return HOUR1;
            }
            case ROLLING60_30: {
                return MINUTE30;
            }
            case ROLLING60_20: {
                return MINUTE20;
            }
            case ROLLING60_15: {
                return MINUTE15;
            }
            case ROLLING60_12: {
                return MINUTE12;
            }
            case ROLLING60_10: {
                return MINUTE10;
            }
            case ROLLING60_6: {
                return MINUTE6;
            }
            case ROLLING60_5: {
                return MINUTE5;
            }
            case ROLLING60_4: {
                return MINUTE4;
            }
            case ROLLING30_15: {
                return MINUTE15;
            }
            case ROLLING30_10: {
                return MINUTE10;
            }
            case ROLLING30_6: {
                return MINUTE6;
            }
            case ROLLING30_5: {
                return MINUTE5;
            }
            case ROLLING30_3: {
                return MINUTE3;
            }
            case ROLLING30_2: {
                return MINUTE2;
            }
            case ROLLING15_5: {
                return MINUTE5;
            }
            case ROLLING15_3: {
                return MINUTE3;
            }
            case ROLLING15_1: {
                return MINUTE1;
            }
            case ROLLING10_5: {
                return MINUTE5;
            }
            case ROLLING10_2: {
                return MINUTE2;
            }
            case ROLLING10_1: {
                return MINUTE1;
            }
            case ROLLING5_1: {
                return MINUTE1;
            }
            case HOUR2: {
                return HOUR2;
            }
            case HOUR3: {
                return HOUR3;
            }
            case HOUR4: {
                return HOUR4;
            }
            case HOUR6: {
                return HOUR6;
            }
            case HOUR12: {
                return HOUR12;
            }
            case SPECIFIEDINTERVAL:  // Intentional fall-through
            case SPECIFIEDFIXEDBLOCK:  // Intentional fall-through
            case SPECIFIEDROLLINGBLOCK:  // Intentional fall-through
            case PRESENT:  // Intentional fall-through
            case PREVIOUS:  // Intentional fall-through
            default: {
                throw new IllegalArgumentException("Unknown or unsupported measurement period: " + measurementPeriod.name());
            }
        }
    }

    private static class TruncFormatModels {
        static final String HOUR = "HH";
        static final String DAY = "DDD";
        static final String WEEK = "DAY";
        static final String MONTH = "MONTH";
        static final String YEAR = "YEAR";
    }

}