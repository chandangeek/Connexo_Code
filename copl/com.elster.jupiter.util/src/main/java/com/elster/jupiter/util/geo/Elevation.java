package com.elster.jupiter.util.geo;

import com.elster.jupiter.util.units.Unit;

import java.math.BigDecimal;

public final class Elevation extends Angle {

    Elevation(BigDecimal value) {
        super(value);
    }

    @Override
    public String toString() {
        return String.valueOf(getValue().intValue()) + " " + Unit.METER;
    }
}
