package com.energyict.dlms.axrdencoding;

import com.energyict.mdc.upl.ProtocolException;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

/**
 * Created by cisac on 5/10/2016.
 */
public class Unsigned64 extends AbstractDataType {

    BigInteger value;
    private static final int LENGTH = 8;
    private static final int SIZE = 9;

    /** Creates a new instance of Enum */
    public Unsigned64(byte[] berEncodedData, int offset) throws IOException {
        if ((berEncodedData[offset] != AxdrType.LONG64_UNSIGNED.getTag())) {
            throw new ProtocolException("Unsigned64, invalid identifier "+berEncodedData[offset]);
        }
        offset++;
        value = new BigInteger(1, Arrays.copyOfRange(berEncodedData, offset, offset+LENGTH));
        offset+=LENGTH;
    }

    public Unsigned64(BigInteger value) {
        this.value=value;
    }

    public String toString() {
        StringBuffer strBuffTab = new StringBuffer();
        for (int i=0;i<getLevel();i++) {
            strBuffTab.append("  ");
        }
        return strBuffTab.toString()+"Unsigned64="+getValue()+"\n";
    }

    protected int size() {
        return SIZE;
    }

    protected byte[] doGetBEREncodedByteArray() {
        byte[] data = new byte[SIZE];
        data[0] = AxdrType.LONG64_UNSIGNED.getTag();
        for (int i=0;i<8;i++) {
            data[i+1] = (byte)(getValue().longValue()>>((7-i)*8));
        }
        return data;
    }

    public BigDecimal toBigDecimal() {
        return new BigDecimal(value);
    }

    public int intValue() {
        return value.intValue();
    }

    public long longValue() {
        return value.longValue();
    }

    public BigInteger getValue() {
        return value;
    }
}
