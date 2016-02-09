package com.elster.insight.usagepoint.config.impl.aggregation;

import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.metering.ReadingType;

import java.time.Duration;
import java.time.Period;
import java.time.temporal.TemporalAmount;

/**
 * Models the length of an interval as specified by the
 * {@link ReadingType#getMacroPeriod()} and {@link ReadingType#getMeasuringPeriod()}
 * of a {@link ReadingType}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-09 (09:27)
 */
public enum IntervalLength {

    MINUTE1(Duration.ofMinutes(1)),
    MINUTE2(Duration.ofMinutes(2)),
    MINUTE3(Duration.ofMinutes(3)),
    MINUTE4(Duration.ofMinutes(4)),
    MINUTE5(Duration.ofMinutes(5)),
    MINUTE6(Duration.ofMinutes(6)),
    MINUTE10(Duration.ofMinutes(10)),
    MINUTE12(Duration.ofMinutes(12)),
    MINUTE15(Duration.ofMinutes(15)),
    MINUTE20(Duration.ofMinutes(20)),
    MINUTE30(Duration.ofMinutes(30)),
    HOUR1(Duration.ofHours(1)),
    HOUR2(Duration.ofHours(2)),
    HOUR3(Duration.ofHours(3)),
    HOUR4(Duration.ofHours(4)),
    HOUR6(Duration.ofHours(6)),
    HOUR12(Duration.ofHours(12)),
    DAY1(Period.ofDays(1)),
    WEEK1(Period.ofWeeks(1)),
    MONTH1(Period.ofMonths(1)),
    YEAR1(Period.ofYears(1));

    private final TemporalAmount temporalAmount;

    IntervalLength(TemporalAmount temporalAmount) {
        this.temporalAmount = temporalAmount;
    }

    public TemporalAmount toTemporalAmount() {
        return temporalAmount;
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

}