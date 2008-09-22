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
public class Unsigned16 extends AbstractDataType {
    
    private int value;
            
    /** Creates a new instance of Enum */
    public Unsigned16(byte[] berEncodedData, int offset) throws IOException {
        if (berEncodedData[offset] != DLMSCOSEMGlobals.TYPEDESC_LONG_UNSIGNED)
            throw new IOException("Unsigned16, invalid identifier "+berEncodedData[offset]);
        offset++;
        setValue(ProtocolUtils.getInt(berEncodedData,offset,2));
        offset+=2;
    }
    
    public String toString() {
        StringBuffer strBuffTab = new StringBuffer();
        for (int i=0;i<getLevel();i++) 
            strBuffTab.append("  ");
        return strBuffTab.toString()+"Unsigned16="+getValue()+"\n";
    }
    
    public Unsigned16(int value) {
        this.value=value;
    }
    
    protected byte[] doGetBEREncodedByteArray() {
        byte[] data = new byte[3];
        data[0] = DLMSCOSEMGlobals.TYPEDESC_LONG_UNSIGNED;
        data[1] = (byte)(getValue()/256);
        data[2] = (byte)(getValue()%256);
        return data;
    }
    
    protected int size() {
        return 3;
    }
    
    static public void main(String[]  artgs) {
        try {
           Unsigned16 v = new Unsigned16(new byte[]{18,1,1}, 0);
           System.out.println(v);
        }
        catch(IOException e) {
            e.printStackTrace();
        }
                
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
        return value;
    }        
}
