package com.energyict.genericprotocolimpl.elster.ctr.object;

import com.energyict.genericprotocolimpl.elster.ctr.primitive.CTRPrimitiveConverter;
import com.energyict.genericprotocolimpl.elster.ctr.primitive.CTRPrimitiveParser;

import java.math.BigDecimal;

/**
 * Created by IntelliJ IDEA.
 * User: khe
 * Date: 21-sep-2010
 * Time: 11:00:24
 */
public abstract class AbstractBCDObject<T extends AbstractBCDObject> extends AbstractCTRObject<T> {

    private CTRAbstractValue[] value; //Binary value, with its unit & an overflowValue.

    //Parse the raw data & fill in the object's properties

    public T parse(byte[] rawData, int offset) {
        CTRPrimitiveParser parser = new CTRPrimitiveParser();   //Not static
        CTRObjectID id = this.getId();
        offset += 2; //Skip the Id bytes

        this.setQlf(parser.parseQlf(rawData, offset));
        offset += 1;

        int[] valueLength = this.parseValueLengths(id);

        this.setValue(parser.parseBCDValue(this, id, rawData, offset, valueLength));
        offset += sum(valueLength);  //There might be multiple value fields

        this.setAccess(parser.parseAccess(rawData, offset));
        offset += 1;

        this.setDefault(parser.parseDefault(id));

        this.setSymbol(parseSymbol(id));

        return (T) this;
    }

    public CTRAbstractValue[] getValue() {
        return value;
    }

    protected void setValue(CTRAbstractValue[] value) {
        this.value = value;
    }

    public byte[] getBytes() {
        CTRPrimitiveConverter converter = new CTRPrimitiveConverter();
        byte[] id = converter.convertId(getId());
        byte[] qlf = converter.convertQlf(getQlf());

        int j = 0;
        byte[] valueBytes = null;
        byte[] valueBytesPrevious = null;
        byte[] valueResult = null;

        for (int valueLength : parseValueLengths(getId())) {

            if (value[j].getType() == "String") {
                valueBytes = converter.convertStringValue((String) value[j].getValue());
            }
            if (value[j].getType() == "BIN") {
                valueBytes = converter.convertBINValue((BigDecimal) value[j].getValue(), valueLength);
            }
            if (value[j].getType() == "UnsignedBIN") {
                valueBytes = converter.convertUnsignedBINValue((BigDecimal) value[j].getValue(), valueLength);
            }
            if (value[j].getType() == "BCD") {
                valueBytes = converter.convertBCDValue((BigDecimal) value[j].getValue());
            }
            if (j > 0) {
                valueResult = concat(valueResult, valueBytes);
            } else {
                valueResult = valueBytes;
            }
            j++;
        }
        byte[] access = converter.convertAccess(getAccess());
        //byte[] def = converter.convertDefaults(getDefault()); 


        return null;
    }


    private byte[] concat(byte[] valueBytesPrevious, byte[] valueBytes) {
        byte[] result = new byte[valueBytesPrevious.length + valueBytes.length];
        System.arraycopy(valueBytesPrevious, 0, result, 0, valueBytesPrevious.length);
        System.arraycopy(valueBytes, 0, result, valueBytesPrevious.length, valueBytes.length);
        return result;
    }

}