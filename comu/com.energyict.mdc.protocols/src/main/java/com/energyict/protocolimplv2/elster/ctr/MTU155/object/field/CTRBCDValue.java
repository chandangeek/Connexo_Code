/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155.object.field;

import com.energyict.mdc.common.Unit;

import java.math.BigDecimal;

/**
 * Created by IntelliJ IDEA.
 * User: khe
 * Date: 22-sep-2010
 * Time: 11:54:19
 * Contains a value field of type BCD
 * Only the getBytes() method is different from other value types
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

    /**
     * Specific for the BCD value
     * @return a byte array representing the value
     */
    public byte[] getBytes() {
        byte[] bts = new byte[value.length() / 2];
        for (int i = 0; i < bts.length; i++) {
            bts[i] = (byte) Integer.parseInt(value.substring(2*i, 2*i+2), 16);
        }
        return bts;
    }

    public int getLength() {
        return valueLength;
    }

    public String getValue() {
        return value;
    }
    public void setValue(Object value) {
        this.value = (String) value;
    }
}