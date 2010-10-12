package com.energyict.genericprotocolimpl.elster.ctr.object.field;

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
    private String value;

    public CTRBCDValue(Unit unit, BigDecimal overflowValue, String value, String type, int valueLenght) {
        this.overflowValue = overflowValue;
        this.unit = unit;
        this.value = value;
        this.type = type;
        this.valueLength = valueLenght;
    }

    public byte[] getBytes() {
        byte[] bts = new byte[value.length() / 2];
        for (int i = 0; i < bts.length; i++) {
            bts[i] = (byte) Integer.parseInt(value.substring(2*i, 2*i+2), 16);
        }
        return bts;
    }

    public String getValue() {
        return value;
    }
    public void setValue(Object value) {
        this.value = (String) value;
    }
}