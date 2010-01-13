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

/**
 *
 * @author kvds
 */
public class Integer8 extends AbstractDataType {

    private int value;

    /** Creates a new instance of Enum */
    public Integer8(byte[] berEncodedData, int offset) throws IOException {
        if (berEncodedData[offset] != DLMSCOSEMGlobals.TYPEDESC_INTEGER) {
			throw new IOException("Integer8, invalid identifier "+berEncodedData[offset]);
		}
        offset++;
        value = (int)berEncodedData[offset];
        offset++;
    }

    public String toString() {
        StringBuffer strBuffTab = new StringBuffer();
        for (int i=0;i<getLevel();i++) {
			strBuffTab.append("  ");
		}
        return strBuffTab.toString()+"Integer8="+getValue()+"\n";
    }

    public Integer8(int value) {
        this.value=value;
    }

    protected byte[] doGetBEREncodedByteArray() {
        byte[] data = new byte[2];
        data[0] = DLMSCOSEMGlobals.TYPEDESC_INTEGER;
        data[1] = (byte)getValue();
        return data;
    }

    protected int size() {
        return 2;
    }

    static public void main(String[]  artgs) {
        try {
           Integer8 v = new Integer8(new byte[]{DLMSCOSEMGlobals.TYPEDESC_INTEGER,(byte)0x81}, 0);
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
        return (int)value;
    }

    public long longValue() {
        return value;
    }
}
