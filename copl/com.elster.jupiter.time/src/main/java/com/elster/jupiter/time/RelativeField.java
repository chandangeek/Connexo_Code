package com.elster.jupiter.time;

import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;

/**
 * Created by borunova on 01.10.2014.
 */
public enum RelativeField {
    YEAR(ChronoField.YEAR),
    MONTH(ChronoUnit.MONTHS),
    WEEK(ChronoUnit.WEEKS),
    DAY(ChronoUnit.DAYS),
    HOUR(ChronoUnit.HOURS),
    MINUTES(ChronoUnit.MINUTES),
    SECONDS(ChronoUnit.SECONDS),
    DAY_IN_WEEK(ChronoField.DAY_OF_WEEK),
    DAY_IN_MONTH(ChronoField.DAY_OF_MONTH),
    MONTH_IN_YEAR(ChronoField.MONTH_OF_YEAR),
    HOUR_OF_DAY(ChronoField.HOUR_OF_DAY),
    MINUTES_OF_HOUR(ChronoField.MINUTE_OF_HOUR),
    STRING_DAY_OF_WEEK(ChronoField.DAY_OF_WEEK);


    private ChronoUnit chronoUnit;
    private ChronoField chronoField;

    private RelativeField(ChronoUnit chronoUnit) {
        this.chronoUnit = chronoUnit;
    }

    private RelativeField(ChronoField chronoField) {
        this.chronoField = chronoField;
    }

    public boolean isChronoUnitBased() {
        return this.getChronoUnit() != null;
    }

    public boolean isChronoFieldBased() {
        return this.getChronoField() != null;
    }

    public ChronoUnit getChronoUnit() {
        return this.chronoUnit;
    }

    public ChronoField getChronoField() {
        return this.chronoField;
    }


    public boolean isValid(long value) {
        return chronoField != null ? chronoField.range().isValidValue(value) : value >= 0;
    }

    public static RelativeField from(int ordinal) {
        return RelativeField.values()[ordinal];
    }
}
