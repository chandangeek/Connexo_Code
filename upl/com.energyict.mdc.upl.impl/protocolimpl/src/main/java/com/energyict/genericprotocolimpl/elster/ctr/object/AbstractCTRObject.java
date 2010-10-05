package com.energyict.genericprotocolimpl.elster.ctr.object;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.genericprotocolimpl.elster.ctr.common.AbstractField;
import com.energyict.genericprotocolimpl.elster.ctr.common.AttributeType;
import com.energyict.genericprotocolimpl.elster.ctr.primitive.CTRPrimitiveConverter;

import java.math.BigDecimal;

/**
 * Created by IntelliJ IDEA.
 * User: khe
 * Date: 21-sep-2010
 * Time: 10:51:36
 */
public abstract class AbstractCTRObject<T extends AbstractCTRObject> {

    private CTRObjectID id;
    private int access;
    private int qlf;
    private String symbol;
    private int[] def;
    private CTRAbstractValue[] value; //Abstract = BIN or String or BCD

    public abstract Unit parseUnit(CTRObjectID id, int valueNumber);
    protected abstract T parse(byte[] rawData, int offset, AttributeType type);
    protected abstract String parseSymbol(CTRObjectID id);
    protected abstract int[] parseValueLengths(CTRObjectID id);
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

    public int getQlf() {
        return qlf;
    }

    protected void setQlf(int qlf) {
        this.qlf = qlf;
    }

    public int getAccess() {
        return access;
    }

    protected void setAccess(int access) {
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
        byte[] id = converter.convertId(getId());
        byte[] bytes = id;

        if (type.hasQualifier()) {
            byte[] qlf = converter.convertQlf(getQlf());
            bytes = concat(id, qlf);
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
                    valueResult = concat(valueResult, valueBytes);
                } else {
                    valueResult = valueBytes;
                }
                j++;
            }
            bytes = concat(bytes, valueResult);
        }

        if (type.hasAccessDescriptor()) {
            byte[] access = converter.convertAccess(getAccess());
            bytes = concat(bytes, access);

        }

        if (type.hasDefaultValue()) {
            byte[] def = converter.convertDefaults(getDefault(), parseValueLengths(getId()));
            bytes = concat(bytes,def);
        }

        return bytes;
    }

    private byte[] concat(byte[] valueBytesPrevious, byte[] valueBytes) {
        byte[] result = new byte[valueBytesPrevious.length + valueBytes.length];
        System.arraycopy(valueBytesPrevious, 0, result, 0, valueBytesPrevious.length);
        System.arraycopy(valueBytes, 0, result, valueBytesPrevious.length, valueBytes.length);
        return result;
    }
}