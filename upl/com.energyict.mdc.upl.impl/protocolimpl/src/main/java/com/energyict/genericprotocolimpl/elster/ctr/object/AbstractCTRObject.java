package com.energyict.genericprotocolimpl.elster.ctr.object;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.genericprotocolimpl.elster.ctr.common.AttributeType;
import com.energyict.genericprotocolimpl.elster.ctr.object.field.*;
import com.energyict.genericprotocolimpl.elster.ctr.primitive.CTRPrimitiveConverter;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.math.BigDecimal;
import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: khe
 * Date: 21-sep-2010
 * Time: 10:51:36
 */
public abstract class AbstractCTRObject<T extends AbstractCTRObject> {

    private CTRObjectID id;
    private AccessDescriptor access;
    private Qualifier qlf;
    private String symbol;
    private int[] def;
    private CTRAbstractValue[] value; //Abstract = BIN or String or BCD

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

    public int getLength(AttributeType type) {
        return getBytes(type).length;          
    }
   
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

    protected void setQlf(Qualifier qlf) {
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

    public int[] getDefault() {
        return def;
    }

    protected void setDefault(int[] def) {
        if (def != null) {
            this.def = def.clone();
        } else {
            this.def = null;
        }
    }

    public CTRAbstractValue[] getValue() {
        return value;
    }

    public CTRAbstractValue getValue(int index) {
        return value[index];
    }

    protected void setValue(CTRAbstractValue[] value) {
        if (value != null) {
            this.value = value.clone();
        } else {
            this.value = null;
        }
    }

    public byte[] getBytes(AttributeType type) {
        CTRPrimitiveConverter converter = new CTRPrimitiveConverter();
        byte[] bytes = new byte[0];

        if (type.hasIdentifier()) {
            bytes = converter.convertId(getId());
        }

        if (type.hasQualifier()) {
            byte[] qlf = converter.convertQlf(getQlf().getQlf());
            bytes = ProtocolTools.concatByteArrays(bytes, qlf);
            if (getQlf().isInvalid()) {
                return bytes;       //Stop here if the qlf indicates the object is invalid
            }
        }

        int[] lengths = getValueLengths(getId());
        if (type.hasValueFields()) {
            byte[] valueBytes = null;
            byte[] valueResult = null;

            for (int i = 0; i < lengths.length; i++) {

                if (CTRAbstractValue.STRING.equals(value[i].getType())) {
                    valueBytes = converter.convertStringValue((String) value[i].getValue(), lengths[i]);
                }
                if (CTRAbstractValue.BIN.equals(value[i].getType())) {
                    valueBytes = converter.convertBINValue((BigDecimal) value[i].getValue(), lengths[i]);
                }
                if (CTRAbstractValue.SIGNEDBIN.equals(value[i].getType())) {
                    valueBytes = converter.convertSignedBINValue((BigDecimal) value[i].getValue(), lengths[i]);
                }
                if (CTRAbstractValue.BCD.equals(value[i].getType())) {
                    valueBytes = converter.convertBCDValue((String) value[i].getValue());
                }

                //also possible? --> valueBytes = value[j].getBytes();

                valueResult = ProtocolTools.concatByteArrays(valueResult, valueBytes);
            }
            bytes = ProtocolTools.concatByteArrays(bytes, valueResult);
        }

        if (type.hasAccessDescriptor()) {
            byte[] access = converter.convertAccess(getAccess().getAccess());
            bytes = ProtocolTools.concatByteArrays(bytes, access);
        }

        if (type.hasDefaultValue()) {
            byte[] def = converter.convertDefaults(getDefault(), lengths);
            bytes = ProtocolTools.concatByteArrays(bytes,def);
        }

        return bytes;
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