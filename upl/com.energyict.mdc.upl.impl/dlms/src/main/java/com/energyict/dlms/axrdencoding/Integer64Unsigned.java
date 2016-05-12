package com.energyict.dlms.axrdencoding;

import com.energyict.protocol.ProtocolException;
import com.energyict.protocol.ProtocolUtils;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * Created by cisac on 5/10/2016.
 */
public class Integer64Unsigned extends AbstractDataType {

    long value;
    public static final int LENGTH = 8;

    /** Creates a new instance of Enum */
    public Integer64Unsigned(byte[] berEncodedData, int offset) throws IOException {
        if ((berEncodedData[offset] != AxdrType.LONG64_UNSIGNED.getTag())) {
            throw new ProtocolException("Integer64, invalid identifier "+berEncodedData[offset]);
        }
        offset++;
        value = ProtocolUtils.getLong(berEncodedData, offset);
        offset+=8;
    }

    public Integer64Unsigned(long value) {
        this.value=value;
    }

    public String toString() {
        StringBuffer strBuffTab = new StringBuffer();
        for (int i=0;i<getLevel();i++) {
            strBuffTab.append("  ");
        }
        return strBuffTab.toString()+"Integer64="+getValue()+"\n";
    }

    protected int size() {
        return 9;
    }

    protected byte[] doGetBEREncodedByteArray() {
        byte[] data = new byte[9];
        data[0] = AxdrType.LONG64_UNSIGNED.getTag();
        for (int i=0;i<8;i++) {
            data[i+1] = (byte)(getValue()>>((7-i)*8));
        }
        return data;
    }

    public BigDecimal toBigDecimal() {
        return BigDecimal.valueOf(getValue());
    }

    public int intValue() {
        return (int)value;
    }

    public long longValue() {
        return value;
    }

    public long getValue() {
        return value;
    }
}
