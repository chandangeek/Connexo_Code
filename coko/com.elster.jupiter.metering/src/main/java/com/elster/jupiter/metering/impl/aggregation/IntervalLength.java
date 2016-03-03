package com.elster.jupiter.metering.impl.aggregation;

import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.util.sql.SqlBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Period;
import java.time.temporal.TemporalAmount;
import java.util.EnumSet;
import java.util.Set;

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
                    IntervalLength.HOUR1,
                    IntervalLength.DAY1,
                    IntervalLength.WEEK1,
                    IntervalLength.MONTH1,
                    IntervalLength.YEAR1);
        }

        @Override
        public BigDecimal getVolumeFlowConversionFactor() {
            return BigDecimal.valueOf(60L);
        }
    },
    MINUTE2(Duration.ofMinutes(2)) {
        @Override
        Set<IntervalLength> multiples() {
            return EnumSet.of(
                    IntervalLength.MINUTE2,
                    IntervalLength.HOUR1,
                    IntervalLength.DAY1,
                    IntervalLength.WEEK1,
                    IntervalLength.MONTH1,
                    IntervalLength.YEAR1);
        }

        @Override
        public BigDecimal getVolumeFlowConversionFactor() {
            return BigDecimal.valueOf(30L);
        }
    },
    MINUTE3(Duration.ofMinutes(3)) {
        @Override
        Set<IntervalLength> multiples() {
            return EnumSet.of(
                    IntervalLength.MINUTE3,
                    IntervalLength.HOUR1,
                    IntervalLength.DAY1,
                    IntervalLength.WEEK1,
                    IntervalLength.MONTH1,
                    IntervalLength.YEAR1);
        }

        @Override
        public BigDecimal getVolumeFlowConversionFactor() {
            return BigDecimal.valueOf(20L);
        }
    },
    MINUTE4(Duration.ofMinutes(4)) {
        @Override
        Set<IntervalLength> multiples() {
            return EnumSet.of(
                    IntervalLength.MINUTE4,
                    IntervalLength.HOUR1,
                    IntervalLength.DAY1,
                    IntervalLength.WEEK1,
                    IntervalLength.MONTH1,
                    IntervalLength.YEAR1);
        }

        @Override
        public BigDecimal getVolumeFlowConversionFactor() {
            return BigDecimal.valueOf(15L);
        }
    },
    MINUTE5(Duration.ofMinutes(5)) {
        @Override
        Set<IntervalLength> multiples() {
            return EnumSet.of(
                    IntervalLength.MINUTE5,
                    IntervalLength.HOUR1,
                    IntervalLength.DAY1,
                    IntervalLength.WEEK1,
                    IntervalLength.MONTH1,
                    IntervalLength.YEAR1);
        }

        @Override
        public BigDecimal getVolumeFlowConversionFactor() {
            return BigDecimal.valueOf(12L);
        }
    },
    MINUTE6(Duration.ofMinutes(6)) {
        @Override
        Set<IntervalLength> multiples() {
            return EnumSet.of(
                    IntervalLength.MINUTE6,
                    IntervalLength.HOUR1,
                    IntervalLength.DAY1,
                    IntervalLength.WEEK1,
                    IntervalLength.MONTH1,
                    IntervalLength.YEAR1);
        }

        @Override
        public BigDecimal getVolumeFlowConversionFactor() {
            return BigDecimal.valueOf(10L);
        }
    },
    MINUTE10(Duration.ofMinutes(10)) {
        @Override
        Set<IntervalLength> multiples() {
            return EnumSet.of(
                    IntervalLength.MINUTE10,
                    IntervalLength.HOUR1,
                    IntervalLength.DAY1,
                    IntervalLength.WEEK1,
                    IntervalLength.MONTH1,
                    IntervalLength.YEAR1);
        }

        @Override
        public BigDecimal getVolumeFlowConversionFactor() {
            return BigDecimal.valueOf(6L);
        }
    },
    MINUTE12(Duration.ofMinutes(12)) {
        @Override
        Set<IntervalLength> multiples() {
            return EnumSet.of(
                    IntervalLength.MINUTE12,
                    IntervalLength.HOUR1,
                    IntervalLength.DAY1,
                    IntervalLength.WEEK1,
                    IntervalLength.MONTH1,
                    IntervalLength.YEAR1);
        }

        @Override
        public BigDecimal getVolumeFlowConversionFactor() {
            return BigDecimal.valueOf(5L);
        }
    },
    MINUTE15(Duration.ofMinutes(15)) {
        @Override
        Set<IntervalLength> multiples() {
            return EnumSet.of(
                    IntervalLength.MINUTE15,
                    IntervalLength.HOUR1,
                    IntervalLength.DAY1,
                    IntervalLength.WEEK1,
                    IntervalLength.MONTH1,
                    IntervalLength.YEAR1);
        }

        @Override
        public BigDecimal getVolumeFlowConversionFactor() {
            return BigDecimal.valueOf(4L);
        }
    },
    MINUTE20(Duration.ofMinutes(20)) {
        @Override
        Set<IntervalLength> multiples() {
            return EnumSet.of(
                    IntervalLength.MINUTE20,
                    IntervalLength.HOUR1,
                    IntervalLength.DAY1,
                    IntervalLength.WEEK1,
                    IntervalLength.MONTH1,
                    IntervalLength.YEAR1);
        }

        @Override
        public BigDecimal getVolumeFlowConversionFactor() {
            return BigDecimal.valueOf(3L);
        }
    },
    MINUTE30(Duration.ofMinutes(30)) {
        @Override
        Set<IntervalLength> multiples() {
            return EnumSet.of(
                    IntervalLength.MINUTE30,
                    IntervalLength.HOUR1,
                    IntervalLength.DAY1,
                    IntervalLength.WEEK1,
                    IntervalLength.MONTH1,
                    IntervalLength.YEAR1);
        }

        @Override
        public BigDecimal getVolumeFlowConversionFactor() {
            return BigDecimal.valueOf(2L);
        }
    },
    HOUR1(Duration.ofHours(1)) {
        @Override
        Set<IntervalLength> multiples() {
            return EnumSet.of(
                    IntervalLength.HOUR1,
                    IntervalLength.DAY1,
                    IntervalLength.WEEK1,
                    IntervalLength.MONTH1,
                    IntervalLength.YEAR1);
        }

        @Override
        String toOracleTruncFormatModel() {
            return TruncFormatModels.HOUR;
        }

        @Override
        public BigDecimal getVolumeFlowConversionFactor() {
            return BigDecimal.ONE;
        }
    },
    HOUR2(Duration.ofHours(2)) {
        @Override
        Set<IntervalLength> multiples() {
            return EnumSet.of(
                    IntervalLength.HOUR2,
                    IntervalLength.DAY1,
                    IntervalLength.WEEK1,
                    IntervalLength.MONTH1,
                    IntervalLength.YEAR1);
        }

        @Override
        public BigDecimal getVolumeFlowConversionFactor() {
            return BigDecimal.ONE.divide(BigDecimal.valueOf(2L), 1, RoundingMode.UNNECESSARY);
        }
    },
    HOUR3(Duration.ofHours(3)) {
        @Override
        Set<IntervalLength> multiples() {
            return EnumSet.of(
                    IntervalLength.HOUR3,
                    IntervalLength.DAY1,
                    IntervalLength.WEEK1,
                    IntervalLength.MONTH1,
                    IntervalLength.YEAR1);
        }

        @Override
        public BigDecimal getVolumeFlowConversionFactor() {
            return BigDecimal.ONE.divide(BigDecimal.valueOf(3L), 20, RoundingMode.DOWN);
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
        String toOracleTruncFormatModel() {
            return TruncFormatModels.DAY;
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
        String toOracleTruncFormatModel() {
            return TruncFormatModels.WEEK;
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
        String toOracleTruncFormatModel() {
            return TruncFormatModels.MONTH;
        }
    },
    YEAR1(Period.ofYears(1)) {
        @Override
        Set<IntervalLength> multiples() {
            return EnumSet.of(IntervalLength.YEAR1);
        }

        @Override
        String toOracleTruncFormatModel() {
            return TruncFormatModels.YEAR;
        }
    },
    NOT_SUPPORTED(Duration.ofMillis(0)) {
        @Override
        protected Set<IntervalLength> multiples() {
            return EnumSet.noneOf(IntervalLength.class);
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
     * Returns the format model for the oracle trunc function
     * that is appropriate for this IntervalLength,
     * i.e. the formal model that will trunc localdate
     * values to this IntervalLength.
     *
     * @return The format model
     */
    String toOracleTruncFormatModel() {
        throw new UnsupportedOperationException(this.name() + " is not supported by the oracle trunc function");
    }

    void appendOracleFormatModelTo(SqlBuilder sqlBuilder) {
        sqlBuilder.append("'");
        sqlBuilder.append(this.toOracleTruncFormatModel());
        sqlBuilder.append("'");
    }

    public BigDecimal getVolumeFlowConversionFactor() {
        throw new UnsupportedOperationException("Volume to flow conversion is not (yet) supported for " + this.name());
    }

    public static IntervalLength from(ReadingType readingType) {
        switch (readingType.getMacroPeriod()) {
            case NOTAPPLICABLE: {
                return fromMeasurementPeriod(readingType.getMeasuringPeriod());
            }
            case DAILY: {
                return DAY1;
            }
            case MONTHLY: {
                return MONTH1;
            }
            case WEEKLYS: {
                return WEEK1;
            }
            case BILLINGPERIOD: // Intentional fall-through
            case SEASONAL: // Intentional fall-through
            case SPECIFIEDPERIOD: // Intentional fall-through
            default: {
                throw new IllegalArgumentException("Unknown or unsupported macro period: " + readingType.getMacroPeriod().name());
            }
        }
    }

    private static IntervalLength fromMeasurementPeriod(TimeAttribute measurementPeriod) {
        switch (measurementPeriod) {
            case NOTAPPLICABLE: {
                throw new IllegalArgumentException("ReadingType must either specify MacroPeriod or MeasurementPeriod");
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
        static final String YEAR = "IYYY";
    }

}