/*
 * Enum.java
 *
 * Created on 16 oktober 2007, 11:35
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.dlms.axrdencoding;

import java.io.IOException;
import java.math.BigDecimal;

import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.protocol.ProtocolUtils;

/**
 *
 * @author kvds
 */
public class Integer16 extends AbstractDataType {
    
    private short value;
            
    /** Creates a new instance of Enum */
    public Integer16(byte[] berEncodedData, int offset) throws IOException {
        if (berEncodedData[offset] != DLMSCOSEMGlobals.TYPEDESC_LONG)
            throw new IOException("Integer16, invalid identifier "+berEncodedData[offset]);
        offset++;
        value = (short)ProtocolUtils.getInt(berEncodedData,offset,2);
        offset+=2;
    }
    
    public String toString() {
        StringBuffer strBuffTab = new StringBuffer();
        for (int i=0;i<getLevel();i++) 
            strBuffTab.append("  ");
        return strBuffTab.toString()+"Integer16="+getValue()+"\n";
    }
    
    public Integer16(int value) {
        this.value=(short)value;
    }
    
    public Integer16(short value) {
        this.value=value;
    }
    
    protected byte[] doGetBEREncodedByteArray() {
        byte[] data = new byte[3];
        data[0] = DLMSCOSEMGlobals.TYPEDESC_LONG;
        data[1] = (byte)(((int)value&0xffff)/256);
        data[2] = (byte)(((int)value&0xffff)%256);
        return data;
    }
    
    protected int size() {
        return 3;
    }
    
    static public void main(String[]  artgs) {
        try {
           Integer16 v = new Integer16(new byte[]{DLMSCOSEMGlobals.TYPEDESC_LONG,(byte)0x80,1}, 0);
           System.out.println(v);
        }
        catch(IOException e) {
            e.printStackTrace();
        }
                
    }

    public int getValue() {
        return value;
    }

    public void setValue(short value) {
        this.value = value;
    }

    public BigDecimal toBigDecimal() {
        return new BigDecimal( value );
    }
    
    public int intValue() {
        return (int)value;
    }
    
    public long longValue() {
        return (long)value;
    }    
}
