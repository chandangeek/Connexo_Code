package com.energyict.genericprotocolimpl.elster.ctr.object.field;

import com.energyict.cbo.Unit;

import java.math.BigDecimal;

/**
 * Created by IntelliJ IDEA.
 * User: khe
 * Date: 22-sep-2010
 * Time: 11:54:19
 * The value field can be string, bin, bcd, ...
 */
public abstract class CTRAbstractValue<T extends Object> {
    protected Unit unit;
    protected BigDecimal overflowValue;
    protected String type;
    protected int valueLength;

    public abstract byte[] getBytes();

    public BigDecimal getOverflowValue() {
        return overflowValue;
    }
    public void setOverflowValue(BigDecimal overflowValue)  {
        this.overflowValue = overflowValue;
    }

    public int getValueLength() {
        return valueLength;
    }

    public void setValueLength(int valueLength) {
        this.valueLength = valueLength;
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
    public abstract T getValue();
    public abstract void setValue(T value);

    @Override
    public String toString() {
        return "CTRAbstractValue{" +
                "overflowValue= " + overflowValue +
                ", unit= " + unit +
                ", type= '" + type + '\'' +
                ", value= '" + getValue() + '\'' +
                '}';
    }
}