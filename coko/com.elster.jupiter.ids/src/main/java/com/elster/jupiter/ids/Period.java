package com.elster.jupiter.ids;

public class Period {
    private final int length;
    private final IntervalLengthUnit unit;

    public Period(int length, IntervalLengthUnit unit) {
        this.length = length;
        this.unit = unit;
    }

    public int getLength() {
        return length;
    }

    public IntervalLengthUnit getUnit() {
        return unit;
    }
}
