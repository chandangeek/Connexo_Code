package com.energyict.genericprotocolimpl.elster.ctr.object.field;

import com.energyict.cbo.Unit;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.math.BigDecimal;

/**
 * Created by IntelliJ IDEA.
 * User: khe
 * Date: 22-sep-2010
 * Time: 11:54:19
 * Contains a value field of type BIN
 */
public class CTRSignedBINValue extends CTRAbstractValue{
    private BigDecimal value;

    public CTRSignedBINValue(Unit unit, BigDecimal overflowValue, BigDecimal value, String type, int valueLength) {
        this.overflowValue = overflowValue;
        this.unit = unit;
        this.value = value;
        this.type = type;
        this.valueLength = valueLength;
    }

    @Override
    public byte[] getBytes() {
        return getBytesFromInt(value.intValue(), valueLength);

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