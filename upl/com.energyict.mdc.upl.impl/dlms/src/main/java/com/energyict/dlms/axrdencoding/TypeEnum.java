/*
 * TypeEnum.java
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
public class TypeEnum extends AbstractDataType {

    private static final int SIZE = 2;
	private int value;

    /**
     * Creates a new instance of TypeEnum
     */
    public TypeEnum(byte[] berEncodedData, int offset) throws IOException {
        if (berEncodedData[offset] != DLMSCOSEMGlobals.TYPEDESC_ENUM) {
			throw new IOException("Enum, invalid identifier "+berEncodedData[offset]);
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
        return strBuffTab.toString()+"TypeEnum="+getValue()+"\n";
    }

    public TypeEnum(int value) {
        this.value=value;
    }

    protected byte[] doGetBEREncodedByteArray() {
        byte[] data = new byte[2];
        data[0] = DLMSCOSEMGlobals.TYPEDESC_ENUM;
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
        return null;
    }

    public int intValue() {
        return value;
    }

    public long longValue() {
        return value;
    }
}
