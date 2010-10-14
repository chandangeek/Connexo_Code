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

    public abstract Unit parseUnit(CTRObjectID id, int valueNumber);
    protected abstract T parse(byte[] rawData, int offset, AttributeType type);
    protected abstract String parseSymbol(CTRObjectID id);
    public abstract int[] parseValueLengths(CTRObjectID id);
    public abstract BigDecimal parseOverflowValue(CTRObjectID id, int valueNumber, Unit unit);


    protected int sum(int[] valueLength) {
        int sum = 0;
        for (int i : valueLength) {
            sum += i;
        }
        return sum;
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

    protected void setValue(CTRAbstractValue[] value) {
        if (value != null) {
            this.value = value.clone();
        } else {
            this.value = null;
        }
    }

    public byte[] getBytes(AttributeType type) {
        CTRPrimitiveConverter converter = new CTRPrimitiveConverter();
        byte[] bytes = null;
        byte[] id = converter.convertId(getId());

        if (type.hasIdentifier()) {
            bytes = id;
        }

        if (type.hasQualifier()) {
            byte[] qlf = converter.convertQlf(getQlf().getQlf());
            bytes = ProtocolTools.concatByteArrays(bytes, qlf);
        }

        if (type.hasValueFields()) {
            int j = 0;
            byte[] valueBytes = null;
            byte[] valueResult = null;

            for (int valueLength : parseValueLengths(getId())) {

                if ("String".equals(value[j].getType())) {
                    valueBytes = converter.convertStringValue((String) value[j].getValue());
                }
                if ("BIN".equals(value[j].getType())) {
                    valueBytes = converter.convertBINValue((BigDecimal) value[j].getValue(), valueLength);
                }
                if ("SignedBIN".equals(value[j].getType())) {
                    valueBytes = converter.convertSignedBINValue((BigDecimal) value[j].getValue(), valueLength);
                }
                if ("BCD".equals(value[j].getType())) {
                    valueBytes = converter.convertBCDValue((String) value[j].getValue());
                }
                if (j > 0) {
                    valueResult = ProtocolTools.concatByteArrays(valueResult, valueBytes);
                } else {
                    valueResult = valueBytes;
                }
                j++;
            }
            bytes = ProtocolTools.concatByteArrays(bytes, valueResult);
        }

        if (type.hasAccessDescriptor()) {
            byte[] access = converter.convertAccess(getAccess().getAccess());
            bytes = ProtocolTools.concatByteArrays(bytes, access);
        }

        if (type.hasDefaultValue()) {
            byte[] def = converter.convertDefaults(getDefault(), parseValueLengths(getId()));
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