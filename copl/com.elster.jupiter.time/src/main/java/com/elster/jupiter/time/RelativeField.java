package com.elster.jupiter.time;

import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

/**
 * Created by borunova on 01.10.2014.
 */
public enum RelativeField {
    YEAR(1, ChronoUnit.YEARS, ChronoField.YEAR),
    MONTH(2, ChronoUnit.MONTHS, ChronoField.MONTH_OF_YEAR),
    WEEK(3, ChronoUnit.WEEKS),
    DAY(4, ChronoUnit.DAYS, ChronoField.DAY_OF_MONTH),
    HOUR(5, ChronoUnit.HOURS, ChronoField.HOUR_OF_DAY),
    MINUTES(6, ChronoUnit.MINUTES, ChronoField.MINUTE_OF_HOUR),
    SECONDS(7, ChronoUnit.SECONDS, ChronoField.SECOND_OF_MINUTE),
    MILLIS(8, ChronoUnit.MILLIS, ChronoField.MILLI_OF_SECOND),
    DAY_OF_WEEK(9, ChronoUnit.DAYS, ChronoField.DAY_OF_WEEK);

    int id;
    private final ChronoUnit chronoUnit;
    private final ChronoField chronoField;

    private RelativeField(int id, ChronoUnit chronoUnit) {
        this(id, chronoUnit, null);
    }

    private RelativeField(int id, ChronoUnit chronoUnit, ChronoField chronoField) {
        this.id = id;
        this.chronoUnit = chronoUnit;
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

    public int getId() {
        return this.id;
    }

    public boolean isValid(long value) {
        return chronoField != null ? chronoField.range().isValidValue(value) : value >= 0;
    }

    public static RelativeField from(int id) {
        return Arrays.stream(values())
                .filter(f -> f.id == id)
                .findAny()
                .orElseThrow(IllegalArgumentException::new);
    }

    public RelativeOperation plus(long value) {
        return new RelativeOperation(this, RelativeOperator.PLUS, value);
    }

    public RelativeOperation minus(long value) {
        return new RelativeOperation(this, RelativeOperator.MINUS, value);
    }

    public RelativeOperation equalTo(long value) {
        return new RelativeOperation(this, RelativeOperator.EQUAL, value);
    }
}
