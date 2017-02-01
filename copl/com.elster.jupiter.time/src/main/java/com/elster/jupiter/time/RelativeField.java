/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.time;

import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.IsoFields;
import java.util.Arrays;

/**
 * Created by borunova on 01.10.2014.
 */
public enum RelativeField {
    YEAR(1, ChronoUnit.YEARS, ChronoField.YEAR, 1),
    MONTH(2, ChronoUnit.MONTHS, ChronoField.MONTH_OF_YEAR, 2),
    WEEK(3, ChronoUnit.WEEKS, 3),
    DAY(4, ChronoUnit.DAYS, ChronoField.DAY_OF_MONTH, 4),
    HOUR(5, ChronoUnit.HOURS, ChronoField.HOUR_OF_DAY, 5),
    MINUTES(6, ChronoUnit.MINUTES, ChronoField.MINUTE_OF_HOUR, 6),
    SECONDS(7, ChronoUnit.SECONDS, ChronoField.SECOND_OF_MINUTE, 7),
    MILLIS(8, ChronoUnit.MILLIS, ChronoField.MILLI_OF_SECOND, 8),
    DAY_OF_WEEK(9, ChronoUnit.DAYS, ChronoField.DAY_OF_WEEK, 4);

    int id;
    private final ChronoUnit chronoUnit;
    private final ChronoField chronoField;
    private Integer priority;
    public static long LAST_DAY_OF_MONTH = 31;

    private RelativeField(int id, ChronoUnit chronoUnit, Integer priority) {
        this(id, chronoUnit, null, priority);
    }

    private RelativeField(int id, ChronoUnit chronoUnit, ChronoField chronoField, Integer priority) {
        this.id = id;
        this.chronoUnit = chronoUnit;
        this.chronoField = chronoField;
        this.priority = priority;
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

    public Integer getPriority() {
        return this.priority;
    }

    public boolean isValid(long value, RelativeOperator operator) {
        if (WEEK.equals(this)) {
            return operator.equals(RelativeOperator.EQUAL) ? IsoFields.WEEK_OF_WEEK_BASED_YEAR.range().isValidValue(value) : value >= 0;
        }
        return operator.equals(RelativeOperator.EQUAL) ? chronoField.range().isValidValue(value) : value >= 0;
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
