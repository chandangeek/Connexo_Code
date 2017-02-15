/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155.object;

import com.energyict.mdc.common.BaseUnit;
import com.energyict.mdc.common.Unit;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AttributeType;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRParsingException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.AccessDescriptor;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.CTRAbstractValue;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.CTRObjectID;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.Default;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.Qualifier;
import com.energyict.protocolimplv2.elster.ctr.MTU155.primitive.CTRPrimitiveConverter;

import java.math.BigDecimal;
import java.util.Arrays;

public abstract class AbstractCTRObject<T extends AbstractCTRObject> {

    protected static final BigDecimal DEFAULT_OVERFLOW = BigDecimal.ZERO;
    protected static final String DEFAULT_SYMBOL = "???";
    protected static final Unit DEFAULT_UNIT = Unit.get(BaseUnit.UNITLESS);

    private CTRObjectID id;
    private AccessDescriptor access;
    private Qualifier qlf;
    private String symbol;
    private Default[] def;
    private CTRAbstractValue[] value; //Abstract = BIN or String or BCD
    private AttributeType type;

    public abstract Unit getUnit(CTRObjectID id, int valueNumber);

    protected abstract T parse(byte[] rawData, int offset, AttributeType type);

    protected abstract String getSymbol(CTRObjectID id);

    public abstract int[] getValueLengths(CTRObjectID id);

    public abstract BigDecimal getOverflowValue(CTRObjectID id, int valueNumber, Unit unit);

    protected int sum(int[] valueLength) {
        int sum = 0;
        for (int i : valueLength) {
            sum += i;
        }
        return sum;
    }

    public int getLength() throws CTRParsingException {
        return getBytes().length;
    }

    /**
     * Checks the unit and finds a matching overflow value if it's a common unit
     * @param unit: the unit to be checked
     * @return the matching overflow value
     */
    protected int getCommonOverflow(Unit unit) {
        int overflow = 0;
        if (Unit.get(BaseUnit.HOUR).equals(unit)) {
            overflow = 24;
        }
        if (Unit.get(BaseUnit.MINUTE).equals(unit)) {
            overflow = 60;
        }
        if (Unit.get(BaseUnit.SECOND).equals(unit)) {
            overflow = 60;
        }
        if (Unit.get(BaseUnit.DAY).equals(unit)) {
            overflow = 32;
        }
        if (Unit.get(BaseUnit.MONTH).equals(unit)) {
            overflow = 12;
        }
        if (Unit.get(BaseUnit.YEAR).equals(unit)) {
            overflow = 99;
        }
        return overflow;
    }

    public CTRObjectID getId() {
        return id;
    }

    protected void setId(CTRObjectID id) {
        this.id = id;
    }

    public Qualifier getQlf() {
        return qlf;
    }

    public void setQlf(Qualifier qlf) {
        this.qlf = qlf;
    }

    public AccessDescriptor getAccess() {
        return access;
    }

    protected void setAccess(AccessDescriptor access) {
        this.access = access;
    }

    public String getSymbol() {
        return symbol;
    }

    protected void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Default[] getDefault() {
        return def;
    }

    protected void setDefault(Default[] def) {
        if (def != null) {
            this.def = def.clone();
        } else {
            this.def = null;
        }
    }

    public CTRAbstractValue[] getValue() {
        return value;
    }

    public CTRAbstractValue getValue(int index) throws IndexOutOfBoundsException {
        if (index >= value.length) {
            throw new IndexOutOfBoundsException("An error happened accessing a value from an array. \nIndex was " + index + ", but the array contains only " + value.length + " element(s)");
        }
        return value[index];
    }

    protected void setValue(CTRAbstractValue[] value) {
        if (value != null) {
            this.value = value.clone();
        } else {
            this.value = null;
        }
    }

    /**
     * Creates a byte array that represents the object.
     * It uses the object's AttributeType description to check the object's relevant fields.
     * @return the byte array
     */
    public byte[] getBytes() {
        CTRPrimitiveConverter converter = new CTRPrimitiveConverter();
        byte[] bytes = new byte[0];

        if (type.hasIdentifier()) {
            bytes = getId().getBytes();
        }

        if (type.hasQualifier()) {
            bytes = ProtocolTools.concatByteArrays(bytes, getQlf().getBytes());
            if (getQlf().isInvalid() && getType().isRegisterQuery()) {
                return bytes;       //Stop here if the qlf indicates the object is invalid and it's a register reading
            }
        }

        int[] lengths = getValueLengths(getId());
        if (type.hasValueFields()) {
            byte[] valueBytes;
            byte[] valueResult = null;
            for (int i = 0; i < lengths.length; i++) {
                valueBytes = value[i].getBytes();
                valueResult = ProtocolTools.concatByteArrays(valueResult, valueBytes);
            }
            bytes = ProtocolTools.concatByteArrays(bytes, valueResult);
        }

        if (type.hasAccessDescriptor()) {
            bytes = ProtocolTools.concatByteArrays(bytes, getAccess().getBytes());
        }

        if (type.hasDefaultValue()) {
            bytes = ProtocolTools.concatByteArrays(bytes, converter.convertDefaults(getDefault(), lengths));
        }

        return bytes;
    }

    public AttributeType getType() {
        return type;
    }

    public void setType(AttributeType type) {
        this.type = new AttributeType(type);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("AbstractCTRObject");
        sb.append("{access=").append(access);
        sb.append(", def=").append(def == null ? "null" : "");
        for (int i = 0; def != null && i < def.length; ++i) {
            sb.append(i == 0 ? "" : ", ").append(def[i]);
        }
        sb.append(", id=").append(id);
        sb.append(", qlf=").append(qlf);
        sb.append(", symbol='").append(symbol).append('\'');
        sb.append(", value=").append(value == null ? "null" : Arrays.asList(value).toString());
        sb.append('}');
        return sb.toString();
    }
}