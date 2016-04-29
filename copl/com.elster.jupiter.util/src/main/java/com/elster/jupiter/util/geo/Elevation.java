package com.elster.jupiter.util.geo;

import java.math.BigDecimal;

public final class Elevation extends Angle {

    Elevation(BigDecimal value) {
        super(value);
    }

    @Override
    public String toString() {
        return String.valueOf(getValue().intValue());
    }
}
