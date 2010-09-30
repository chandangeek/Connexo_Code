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
public abstract class CTRAbstractValue {
    protected Unit unit;
    protected BigDecimal overflowValue;
    protected String type;
    
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

    public String getType() {
        return type;
    }
    public abstract Object getValue();
    public abstract void setValue(Object value);

    
}