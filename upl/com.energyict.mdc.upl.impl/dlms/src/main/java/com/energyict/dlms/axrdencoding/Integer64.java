package com.energyict.dlms.axrdencoding;

import com.energyict.dlms.*;
import com.energyict.protocol.*;
import java.io.*;
import java.math.*;

public class Integer64 extends AbstractDataType {
    
    long value;

    /** Creates a new instance of Enum */
    public Integer64(byte[] berEncodedData, int offset) throws IOException {
        if (berEncodedData[offset] != DLMSCOSEMGlobals.TYPEDESC_LONG64)
            throw new IOException("Integer64, invalid identifier "+berEncodedData[offset]);
        offset++;
        value = ProtocolUtils.getLong(berEncodedData,offset);
        offset+=8;
    }
    
    public Integer64(long value) {
        this.value=value;
    }    
    
    public String toString() {
        StringBuffer strBuffTab = new StringBuffer();
        for (int i=0;i<getLevel();i++) 
            strBuffTab.append("  ");
        return strBuffTab.toString()+"Integer64="+getValue()+"\n";
    }    
    
    protected int size() {
        return 9;
    }
    
    protected byte[] doGetBEREncodedByteArray() {
        byte[] data = new byte[9];
        data[0] = DLMSCOSEMGlobals.TYPEDESC_LONG64;
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