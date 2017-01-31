/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155.object.field;

import com.energyict.mdc.common.BaseUnit;
import com.energyict.mdc.common.Unit;

public class Default {
    private int defaultValue;
    private Unit unit = Unit.get(BaseUnit.UNITLESS);


    public int getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(int defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public Default(int defaultValue, Unit unit) {
        this.defaultValue = defaultValue;
        this.unit = unit;
    }

    @Override
    public String toString() {
        return Integer.toString(getDefaultValue());
    }

}
