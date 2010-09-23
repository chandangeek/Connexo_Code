package com.energyict.genericprotocolimpl.elster.ctr.object;

import com.energyict.cbo.Unit;

import java.math.BigDecimal;

/**
 * Created by IntelliJ IDEA.
 * User: khe
 * Date: 22-sep-2010
 * Time: 11:54:19
 */
public class CTRObjectValue {
    private BigDecimal value;
    private Unit unit;
    private BigDecimal overflowValue;

    public CTRObjectValue(BigDecimal overflowValue, Unit unit, BigDecimal value) {
        this.overflowValue = overflowValue;
        this.unit = unit;
        this.value = value;
    }

    public BigDecimal getValue() {
        return value;
    }
    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public BigDecimal getOverflowValue() {
        return overflowValue;
    }
    public void setOverflowValue(BigDecimal overflowValue)  {
        this.overflowValue = overflowValue;
    }
     
    public Unit getUnit() {
        return unit;
    }
    public void setUnit(Unit unit) {
        this.unit = unit;
    }
    
}