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
public class NullData extends AbstractDataType {

    /** Creates a new instance of Enum */
    public NullData(byte[] berEncodedData, int offset) throws IOException {
        if (berEncodedData[offset] != 0) {
			throw new IOException("Nulldata, invalid identifier "+berEncodedData[offset]);
		}
        offset++;
    }

    public String toString() {
        StringBuffer strBuffTab = new StringBuffer();
        for (int i=0;i<getLevel();i++) {
			strBuffTab.append("  ");
		}
        return strBuffTab.toString()+"NullData\n";
    }

    public NullData() {
    }

    protected byte[] doGetBEREncodedByteArray() {
        byte[] data = new byte[1];
        data[0] = 0;
        return data;
    }

    protected int size() {
        return 1;
    }

    static public void main(String[]  artgs) {
        try {
           NullData v = new NullData(new byte[]{DLMSCOSEMGlobals.TYPEDESC_NULL,(byte)0x81}, 0);
           System.out.println(v);
        }
        catch(IOException e) {
            e.printStackTrace();
        }

    }

    public BigDecimal toBigDecimal() {
        return new BigDecimal("0");
    }
    public int intValue() {
        return 0;
    }

    public long longValue() {
        return 0;
    }
}
