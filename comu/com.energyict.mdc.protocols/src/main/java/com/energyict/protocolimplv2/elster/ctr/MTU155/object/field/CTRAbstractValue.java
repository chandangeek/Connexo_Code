/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155.object.field;

import com.energyict.mdc.common.BaseUnit;
import com.energyict.mdc.common.Unit;

import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AbstractField;
import com.energyict.protocolimplv2.elster.ctr.MTU155.common.Field;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRParsingException;

import java.math.BigDecimal;

/**
 * Created by IntelliJ IDEA.
 * User: khe
 * Date: 22-sep-2010
 * Time: 11:54:19
 * The value field can be string, bin, bcd, ...
 */
public abstract class CTRAbstractValue<T extends Object> extends AbstractField {

    protected Unit unit = Unit.get(BaseUnit.UNITLESS);
    protected BigDecimal overflowValue;
    protected String type;
    protected int valueLength;

    public static final String STRING = "String";
    public static final String SIGNEDBIN = "SignedBIN";
    public static final String BCD = "BCD";
    public static final String BIN = "BIN";

    public abstract byte[] getBytes();

    public Field parse(byte[] rawData, int offset) throws CTRParsingException {
        return this;
    }

    public BigDecimal getOverflowValue() {
        return overflowValue;
    }

    public void setOverflowValue(BigDecimal overflowValue) {
        this.overflowValue = overflowValue;
    }

    public int getValueLength() {
        return valueLength;
    }

    public void setValueLength(int valueLength) {
        this.valueLength = valueLength;
    }

    public Unit getUnit() {
        if (unit == null) {
            return Unit.getUndefined();
        }
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public String getType() {
        return type;
    }

    public int getIntValue() {
        if (getValue() instanceof Number) {
            return ((Number) getValue()).intValue();
        } else {
            return -1;
        }
    }

    public String getStringValue() {
        if (getValue() instanceof String) {
            return (String) getValue();
        } else if (getValue() instanceof Number) {
            return String.valueOf(((Number)getValue()).floatValue());
        } else {
            return getValue().toString();
        }
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