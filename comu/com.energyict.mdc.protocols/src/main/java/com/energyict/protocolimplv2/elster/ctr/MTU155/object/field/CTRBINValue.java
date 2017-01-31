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
 * Contains a value field of type BIN
 */
public class CTRBINValue extends CTRAbstractValue{
    private BigDecimal value;

    public CTRBINValue(Unit unit, BigDecimal overflowValue, BigDecimal value, String type, int valueLength) {
        this.overflowValue = overflowValue;
        this.unit = unit;
        this.value = value;
        this.type = type;
        this.valueLength = valueLength;
    }

    /**
     * returns a byte array representing the value
     * @return byte array
     */
    @Override
    public byte[] getBytes() {
        byte[] result = new byte[valueLength];
        for (int i = (valueLength - 1); i >= 0; i--) {
            BigDecimal divider = new BigDecimal(Math.pow(256, i));
            result[valueLength - 1 - i] =  (byte) ((value.divide(divider)).intValue() & 0xFF);
        }
        return result;
    }


    public int getLength() {
        return valueLength;
    }

    public BigDecimal getValue() {
        return value;
    }
    public void setValue(Object value) {
        this.value = (BigDecimal) value;
    }

    public String toString() {
        return ProtocolTools.getHexStringFromBytes(getBytes(), "");
    }
}