package com.energyict.dlms.axrdencoding;

import java.io.IOException;
import java.math.BigDecimal;

import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.protocol.ProtocolUtils;

public class Integer32 extends AbstractDataType {

    int value;

    /** Creates a new instance of Enum */
    public Integer32(byte[] berEncodedData, int offset) throws IOException {
        if (berEncodedData[offset] != DLMSCOSEMGlobals.TYPEDESC_DOUBLE_LONG) {
			throw new IOException("Integer32, invalid identifier "+berEncodedData[offset]);
		}
        offset++;
        value = ProtocolUtils.getInt(berEncodedData,offset);
        offset+=4;
    }

    public Integer32(int value) {
        this.value=value;
    }

    public String toString() {
        StringBuffer strBuffTab = new StringBuffer();
        for (int i=0;i<getLevel();i++) {
			strBuffTab.append("  ");
		}
        return strBuffTab.toString()+"Integer32="+getValue()+"\n";
    }

    protected int size() {
        return 5;
    }

    protected byte[] doGetBEREncodedByteArray() {
        byte[] data = new byte[5];
        data[0] = DLMSCOSEMGlobals.TYPEDESC_DOUBLE_LONG;
        for (int i=0;i<4;i++) {
           data[i+1] = (byte)(getValue()>>((3-i)*8));
        }
        return data;
    }

    public BigDecimal toBigDecimal() {
        return BigDecimal.valueOf(getValue());
    }

    public int intValue() {
        return value;
    }

    public long longValue() {
        return (long)value;
    }

    public int getValue() {
        return value;
    }
}