package com.elster.insight.usagepoint.config.impl.aggregation;

import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.metering.ReadingType;

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
        protected Set<IntervalLength> multiples() {
            return EnumSet.allOf(IntervalLength.class);
        }
    },
    MINUTE2(Duration.ofMinutes(2)) {
        @Override
        protected Set<IntervalLength> multiples() {
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
                    IntervalLength.HOUR12,
                    IntervalLength.DAY1,
                    IntervalLength.WEEK1,
                    IntervalLength.MONTH1,
                    IntervalLength.YEAR1);
        }
    },
    MINUTE3(Duration.ofMinutes(3)) {
        @Override
        protected Set<IntervalLength> multiples() {
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
                    IntervalLength.HOUR12,
                    IntervalLength.DAY1,
                    IntervalLength.WEEK1,
                    IntervalLength.MONTH1,
                    IntervalLength.YEAR1);
        }
    },
    MINUTE4(Duration.ofMinutes(4)) {
        @Override
        protected Set<IntervalLength> multiples() {
            return EnumSet.of(
                    IntervalLength.MINUTE4,
                    IntervalLength.MINUTE12,
                    IntervalLength.MINUTE20,
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
    },
    MINUTE5(Duration.ofMinutes(5)) {
        @Override
        protected Set<IntervalLength> multiples() {
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
                    IntervalLength.HOUR12,
                    IntervalLength.DAY1,
                    IntervalLength.WEEK1,
                    IntervalLength.MONTH1,
                    IntervalLength.YEAR1);
        }
    },
    MINUTE6(Duration.ofMinutes(6)) {
        @Override
        protected Set<IntervalLength> multiples() {
            return EnumSet.of(
                    IntervalLength.MINUTE6,
                    IntervalLength.MINUTE12,
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
    },
    MINUTE10(Duration.ofMinutes(10)) {
        @Override
        protected Set<IntervalLength> multiples() {
            return EnumSet.of(
                    IntervalLength.MINUTE10,
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
    },
    MINUTE12(Duration.ofMinutes(12)) {
        @Override
        protected Set<IntervalLength> multiples() {
            return EnumSet.of(
                    IntervalLength.MINUTE12,
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
    },
    MINUTE15(Duration.ofMinutes(15)) {
        @Override
        protected Set<IntervalLength> multiples() {
            return EnumSet.of(
                    IntervalLength.MINUTE15,
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
    },
    MINUTE20(Duration.ofMinutes(20)) {
        @Override
        protected Set<IntervalLength> multiples() {
            return EnumSet.of(
                    IntervalLength.MINUTE20,
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
    },
    MINUTE30(Duration.ofMinutes(30)) {
        @Override
        protected Set<IntervalLength> multiples() {
            return EnumSet.of(
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
    },
    HOUR1(Duration.ofHours(1)) {
        @Override
        protected Set<IntervalLength> multiples() {
            return EnumSet.of(
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
    },
    HOUR2(Duration.ofHours(2)) {
        @Override
        protected Set<IntervalLength> multiples() {
            return EnumSet.of(
                    IntervalLength.HOUR2,
                    IntervalLength.HOUR4,
                    IntervalLength.HOUR6,
                    IntervalLength.HOUR12,
                    IntervalLength.DAY1,
                    IntervalLength.WEEK1,
                    IntervalLength.MONTH1,
                    IntervalLength.YEAR1);
        }
    },
    HOUR3(Duration.ofHours(3)) {
        @Override
        protected Set<IntervalLength> multiples() {
            return EnumSet.of(
                    IntervalLength.HOUR3,
                    IntervalLength.HOUR6,
                    IntervalLength.HOUR12,
                    IntervalLength.DAY1,
                    IntervalLength.WEEK1,
                    IntervalLength.MONTH1,
                    IntervalLength.YEAR1);
        }
    },
    HOUR4(Duration.ofHours(4)) {
        @Override
        protected Set<IntervalLength> multiples() {
            return EnumSet.of(
                    IntervalLength.HOUR4,
                    IntervalLength.HOUR12,
                    IntervalLength.DAY1,
                    IntervalLength.WEEK1,
                    IntervalLength.MONTH1,
                    IntervalLength.YEAR1);
        }
    },
    HOUR6(Duration.ofHours(6)) {
        @Override
        protected Set<IntervalLength> multiples() {
            return EnumSet.of(
                    IntervalLength.HOUR6,
                    IntervalLength.HOUR12,
                    IntervalLength.DAY1,
                    IntervalLength.WEEK1,
                    IntervalLength.MONTH1,
                    IntervalLength.YEAR1);
        }
    },
    HOUR12(Duration.ofHours(12)) {
        @Override
        protected Set<IntervalLength> multiples() {
            return EnumSet.of(
                    IntervalLength.HOUR12,
                    IntervalLength.DAY1,
                    IntervalLength.WEEK1,
                    IntervalLength.MONTH1,
                    IntervalLength.YEAR1);
        }
    },
    DAY1(Period.ofDays(1)) {
        @Override
        protected Set<IntervalLength> multiples() {
            return EnumSet.of(
                    IntervalLength.DAY1,
                    IntervalLength.WEEK1,
                    IntervalLength.MONTH1,
                    IntervalLength.YEAR1);
        }
    },
    WEEK1(Period.ofWeeks(1)) {
        @Override
        protected Set<IntervalLength> multiples() {
            return EnumSet.of(
                    IntervalLength.WEEK1,
                    IntervalLength.MONTH1,
                    IntervalLength.YEAR1);
        }
    },
    MONTH1(Period.ofMonths(1)) {
        @Override
        protected Set<IntervalLength> multiples() {
            return EnumSet.of(
                    IntervalLength.MONTH1,
                    IntervalLength.YEAR1);
        }
    },
    YEAR1(Period.ofYears(1)) {
        @Override
        protected Set<IntervalLength> multiples() {
            return EnumSet.of(IntervalLength.YEAR1);
        }
    };

    private final TemporalAmount temporalAmount;

    IntervalLength(TemporalAmount temporalAmount) {
        this.temporalAmount = temporalAmount;
    }

    public TemporalAmount toTemporalAmount() {
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
    public boolean isMultipleOf(IntervalLength other) {
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
    public boolean multipliesTo(IntervalLength other) {
        return this.multiples().contains(other);
    }

    /**
     * Returns all the multiples of this IntervalLength,
     * i.e. all the IntervalLength to which this one can be multiplied.
     *
     * @return The multiples
     */
    protected abstract Set<IntervalLength> multiples();

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

}