/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cbo;

import com.elster.jupiter.util.units.Quantity;

import java.math.BigDecimal;

public enum MetricMultiplier {
    YOCTO(-24, "y"),
    ZEPTO(-21, "z"),
    ATTO(-18, "a"),
    FEMTO(-15, "f"),
    PICO(-12, "p"),
    NANO(-9, "n"),
    MICRO(-6, "\u00b5"),
    MILLI(-3, "m"),
    CENTI(-2, "c"),
    DECI(-1, "d"),
    ZERO(0, ""),
    DECA(1, "Da"),
    HECTO(2, "h"),
    KILO(3, "k"),
    MEGA(6, "M"),
    GIGA(9, "G"),
    TERA(12, "T"),
    PETA(15, "P"),
    EXA(18, "E"),
    ZETTA(21, "Z"),
    YOTTA(24, "Y");

    private final int multiplier;
    private final String symbol;

    MetricMultiplier(int multiplier, String symbol) {
        this.multiplier = multiplier;
        this.symbol = symbol;
    }

    public static MetricMultiplier with(int multiplier) {
        for (MetricMultiplier each : values()) {
            if (each.multiplier == multiplier) {
                return each;
            }
        }
        throw new IllegalEnumValueException(MetricMultiplier.class, multiplier);
    }

    public int getMultiplier() {
        return multiplier;
    }

    public String getSymbol() {
        return symbol;
    }

    @Override
    public String toString() {
        return "*10^" + multiplier;
    }

    public static Quantity quantity(BigDecimal value, MetricMultiplier multiplier, ReadingTypeUnit unit) {
        return unit.getUnit().amount(value, multiplier.getMultiplier());
    }
}
