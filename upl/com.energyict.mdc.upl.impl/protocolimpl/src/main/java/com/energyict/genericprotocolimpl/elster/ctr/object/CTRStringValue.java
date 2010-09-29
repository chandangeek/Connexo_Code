package com.energyict.genericprotocolimpl.elster.ctr.object;

import com.energyict.cbo.Unit;

import java.math.BigDecimal;

/**
 * Created by IntelliJ IDEA.
 * User: khe
 * Date: 22-sep-2010
 * Time: 11:54:19
 * Contains a value field of type String
 */
public class CTRStringValue extends CTRAbstractValue{
    private String value;

    public CTRStringValue(Unit unit, BigDecimal overflowValue, String value) {
        this.overflowValue = overflowValue;
        this.unit = unit;
        this.value = value;
    }

    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }
}