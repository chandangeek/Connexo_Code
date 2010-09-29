package com.energyict.genericprotocolimpl.elster.ctr.object;

import com.energyict.cbo.Unit;

import java.math.BigDecimal;

/**
 * Created by IntelliJ IDEA.
 * User: khe
 * Date: 22-sep-2010
 * Time: 11:54:19
 * Contains a value field of type BIN
 */
public class CTRBCDValue extends CTRAbstractValue{
    private BigDecimal value;

    public CTRBCDValue(Unit unit, BigDecimal overflowValue, BigDecimal value) {
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
}