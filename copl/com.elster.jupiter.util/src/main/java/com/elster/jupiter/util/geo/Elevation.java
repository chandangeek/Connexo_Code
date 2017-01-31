/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.geo;

import com.elster.jupiter.util.units.Unit;

import java.math.BigDecimal;

public final class Elevation extends Angle {

    public Elevation(BigDecimal value) {
        super(value);
    }

    @Override
    public String toString() {
        return String.valueOf(getValue().intValue()) + " " + Unit.METER;
    }
}
