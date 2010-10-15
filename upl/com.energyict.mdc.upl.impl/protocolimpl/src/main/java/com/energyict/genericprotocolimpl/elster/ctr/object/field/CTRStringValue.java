package com.energyict.genericprotocolimpl.elster.ctr.object.field;

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

    public CTRStringValue(Unit unit, BigDecimal overflowValue, String value, String type, int valueLenght) {
        this.overflowValue = overflowValue;
        this.unit = unit;
        this.value = value;
        this.type = type;
        this.valueLength = valueLenght;
    }

    @Override
    public byte[] getBytes() {
/*
        byte[] bytes = new byte[valueLength];
        System.arraycopy(value.getBytes(), 0, bytes, 0, value.length());
        return bytes;
*/
        return value.getBytes();
    }

    public String getValue() {
/*
        return value.replace("\0", "");
*/
        return value;
    }

    public void setValue(Object value) {
        this.value = (String) value;
    }
}