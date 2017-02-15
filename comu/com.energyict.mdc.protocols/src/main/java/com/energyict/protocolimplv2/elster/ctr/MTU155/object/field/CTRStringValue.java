/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155.object.field;

import com.energyict.mdc.common.Unit;

import com.energyict.protocolimpl.utils.ProtocolTools;

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

    /**
     * returns a byte array representing the value
     * @return byte array
     */
    @Override
    public byte[] getBytes() {
        byte[] bytes = new byte[valueLength];
        System.arraycopy(value.getBytes(), 0, bytes, 0, value.length());
        return bytes;
    }

    public int getLength() {
        return valueLength;
    }

    public String getValue() {
        return ProtocolTools.getAsciiFromBytes(value.getBytes(), ' ').trim();
    }

    public void setValue(Object value) {
        this.value = (String) value;
    }
}