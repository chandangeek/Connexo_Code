/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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

import com.energyict.mdc.protocol.api.ProtocolException;

import java.io.IOException;
import java.math.BigDecimal;

/**
 *
 * @author kvds
 */
public class NullData extends AbstractDataType {

    /** Creates a new instance of Enum */
    public NullData(byte[] berEncodedData, int offset) throws IOException {
        if (berEncodedData[offset] != 0) {
			throw new ProtocolException("Nulldata, invalid identifier "+berEncodedData[offset]);
		}
        offset++;
    }

    public String toString() {
        StringBuilder strBuffTab = new StringBuilder();
        for (int i=0;i<getLevel();i++) {
			strBuffTab.append("  ");
		}
        strBuffTab.append("NullData\n");
        return strBuffTab.toString();
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