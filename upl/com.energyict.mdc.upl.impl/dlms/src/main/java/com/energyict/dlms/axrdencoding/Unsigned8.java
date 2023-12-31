/*
 * Enum.java
 *
 * Created on 16 oktober 2007, 11:35
 *
 */

package com.energyict.dlms.axrdencoding;

import com.energyict.mdc.upl.ProtocolException;

import java.io.IOException;
import java.math.BigDecimal;

/**
 *
 * @author kvds
 */
public class Unsigned8 extends AbstractDataType {

    public static final int SIZE = 2;
    private int value;

    /** Creates a new instance of Enum */
    public Unsigned8(byte[] berEncodedData, int offset) throws IOException {
        if (berEncodedData[offset] != AxdrType.UNSIGNED.getTag()) {
			throw new ProtocolException("Unsigned8, invalid identifier "+berEncodedData[offset]);
		}
        offset++;
        setValue(berEncodedData[offset++]&0xff);
        offset++;
    }

    public String toString() {
        StringBuffer strBuffTab = new StringBuffer();
        for (int i=0;i<getLevel();i++) {
			strBuffTab.append("  ");
		}
        return strBuffTab.toString()+"Unsigned8="+getValue()+"\n";
    }

    public Unsigned8(int value) {
        this.value=value;
    }

    protected byte[] doGetBEREncodedByteArray() {
        byte[] data = new byte[2];
        data[0] = AxdrType.UNSIGNED.getTag();
        data[1] = (byte)getValue();
        return data;
    }

    protected int size() {
        return SIZE;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public BigDecimal toBigDecimal() {
        return new BigDecimal( value );
    }

    public int intValue() {
        return value;
    }

    public long longValue() {
        return (long)value&0xFF;
    }

}
