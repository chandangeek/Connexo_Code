package com.energyict.protocolimplv2.elster.ctr.MTU155.object.field;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;

/**
 * Class for the default field in a CTR Object
 * Copyrights EnergyICT
 * Date: 20-okt-2010
 * Time: 9:59:37
 */
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
