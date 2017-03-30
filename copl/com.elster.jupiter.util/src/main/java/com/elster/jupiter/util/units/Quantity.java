/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.units;

import com.elster.jupiter.util.Checks;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.math.BigDecimal;
import java.util.Objects;

@XmlRootElement
@XmlJavaTypeAdapter(QuantityAdapter.class)

/**
 * Immutable class representing a quantity in one unit.
 */
public final class Quantity implements Comparable<Quantity> {

    private static final int YOCTO = -24;
    private static final int ZEPTO = -21;
    private static final int ATTO = -18;
    private static final int FEMTO = -15;
    private static final int PICO = -12;
    private static final int NANO = -9;
    private static final int MICRO = -6;
    private static final int MILLI = -3;
    private static final int CENTI = -2;
    private static final int DECI = -1;
    private static final int DECA = 1;
    private static final int HECTO = 2;
    private static final int KILO = 3;
    private static final int MEGA = 6;
    private static final int GIGA = 9;
    private static final int TERA = 12;
    private static final int PETA = 15;
    private static final int EXA = 18;
    private static final int ZETTA = 21;
    private static final int YOTTA = 24;

    private final Unit unit;
    private final BigDecimal value;
    private final int multiplier;

    @SuppressWarnings("unused")
    private Quantity() {
        unit = null;
        value = null;
        multiplier = 0;
    }

    Quantity(Unit unit, BigDecimal value, int multiplier) {
        this.unit = Objects.requireNonNull(unit, "unit cannot be null");
        this.value = Objects.requireNonNull(value, "value cannot be null");
        this.multiplier = multiplier;
    }

    Quantity(Unit unit, BigDecimal value) {
        this(unit, value, 0);
    }

    public Unit getUnit() {
        return unit;
    }

    public BigDecimal getValue() {
        return value;
    }

    public int getMultiplier() {
        return multiplier;
    }

    private String getCode(int exponent, boolean asciiOnly) {
        switch (exponent) {
            case YOCTO:
                return "y";
            case ZEPTO:
                return "z";
            case ATTO:
                return "a";
            case FEMTO:
                return "f";
            case PICO:
                return "p";
            case NANO:
                return "n";
            case MICRO:
                return asciiOnly ? "micro" : "\u00b5";
            case MILLI:
                return "m";
            case CENTI:
                return "c";
            case DECI:
                return "d";
            case 0:
                return "";
            case DECA:
                return "Da";
            case HECTO:
                return "h";
            case KILO:
                return "k";
            case MEGA:
                return "M";
            case GIGA:
                return "G";
            case TERA:
                return "T";
            case PETA:
                return "P";
            case EXA:
                return "E";
            case ZETTA:
                return "Z";
            case YOTTA:
                return "Y";
            default:
                return "*10^" + exponent;
        }
    }

    public Quantity asSi() {
        if (unit.isDimensionLess() || unit.isCoherentSiUnit()) {
            return this;
        }
        return unit.siValue(value.scaleByPowerOfTen(multiplier));
    }

    @Override
    public String toString() {
        return "" + value + " " + getCode(multiplier, false) + unit.getSymbol();
    }

    public String toString(boolean asciiOnly) {
        return "" + value + " " + getCode(multiplier, asciiOnly) + unit.getSymbol(asciiOnly);
    }

    public static Quantity create(BigDecimal value, int multiplier, String unitSymbol) {
        Unit unit = Unit.get(unitSymbol);
        if (unit == null) {
            throw new IllegalArgumentException(unitSymbol);
        }
        return unit.amount(value, multiplier);
    }

    public static Quantity create(BigDecimal value, String unitSymbol) {
        return create(value, 0, unitSymbol);
    }

    @Override
    public int compareTo(Quantity o) {
        if (!unit.equals(o.unit)) {
            throw new IllegalArgumentException();
        }
        return asSi().getValue().compareTo(o.asSi().getValue());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Quantity quantity = (Quantity) o;

        return multiplier == quantity.multiplier && unit == quantity.unit && Checks.is(value)
                .equalValue(quantity.value);

    }

    @Override
    public int hashCode() {
        return Objects.hash(multiplier, unit, value.doubleValue());
    }
}
