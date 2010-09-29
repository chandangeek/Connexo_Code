package com.energyict.genericprotocolimpl.elster.ctr.object;

import com.energyict.cbo.Unit;

import java.math.BigDecimal;

/**
 * Created by IntelliJ IDEA.
 * User: khe
 * Date: 22-sep-2010
 * Time: 11:54:19
 * The value field can be string, bin, bcd, ...
 */
public class CTRAbstractValue {
    protected Unit unit;
    protected BigDecimal overflowValue;

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