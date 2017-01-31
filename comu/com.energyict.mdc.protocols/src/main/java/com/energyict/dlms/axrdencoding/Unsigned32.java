/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.axrdencoding;

import com.energyict.mdc.protocol.api.ProtocolException;
import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;
import java.math.BigDecimal;

public class Unsigned32 extends AbstractDataType {

    private static final int SIZE = 5;
	private long value;

    /** Creates a new instance of Enum */
    public Unsigned32(byte[] berEncodedData, int offset) throws IOException {
        if (berEncodedData[offset] != AxdrType.DOUBLE_LONG_UNSIGNED.getTag()) {
			throw new ProtocolException("Unsigned32, invalid identifier "+berEncodedData[offset]);
		}
        offset++;
        setValue(ProtocolUtils.getLong(berEncodedData,offset,4));
        offset+=4;
    }

    public String toString() {
        StringBuffer strBuffTab = new StringBuffer();
        for (int i=0;i<getLevel();i++) {
			strBuffTab.append("  ");
		}
        return strBuffTab.toString()+"Unsigned32="+getValue()+"\n";
    }

    public Unsigned32(long value) {
        this.value=value;
    }

    public Unsigned32(int value) {
        this.value=value;
    }

    protected byte[] doGetBEREncodedByteArray() {
        byte[] data = new byte[5];
        data[0] = AxdrType.DOUBLE_LONG_UNSIGNED.getTag();
        data[1] = (byte)(getValue() >> 24 );
        data[2] = (byte)(getValue() >> 16 );
        data[3] = (byte)(getValue() >> 8 );
        data[4] = (byte)(getValue() );
        return data;
    }

    protected int size() {
        return SIZE;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public BigDecimal toBigDecimal() {
        return new BigDecimal( value );
    }

    public int intValue() {
        return (int)value;
    }

    public long longValue() {
        return value;
    }
}
