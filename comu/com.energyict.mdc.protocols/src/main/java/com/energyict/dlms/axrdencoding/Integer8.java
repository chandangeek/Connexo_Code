/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.axrdencoding;

import com.energyict.mdc.protocol.api.ProtocolException;

import java.io.IOException;
import java.math.BigDecimal;

/**
 *
 * @author kvds
 */
public class Integer8 extends AbstractDataType {

    private int value;

    /**
     * @param berEncodedData
     * @param offset
     * @throws IOException
     */
    public Integer8(byte[] berEncodedData, int offset) throws IOException {
        if (berEncodedData[offset] != AxdrType.INTEGER.getTag()) {
			throw new ProtocolException("Integer8, invalid identifier "+berEncodedData[offset]);
		}
        offset++;
        value = berEncodedData[offset];
        offset++;
    }

    /**
     * Create a new {@link Integer8} with a given value
     * @param value
     */
    public Integer8(int value) {
        this.value=value;
    }

    @Override
	protected byte[] doGetBEREncodedByteArray() {
        byte[] data = new byte[size()];
        data[0] = AxdrType.INTEGER.getTag();
        data[1] = (byte)getValue();
        return data;
    }

    @Override
	protected int size() {
        return 2;
    }

    /**
     * Get the current value of the {@link Integer8} object
     * @return
     */
    public int getValue() {
        return value;
    }

    /**
     * Set the current value of the {@link Integer8} object
     * @param value
     */
    public void setValue(int value) {
        this.value = value;
    }

    @Override
	public BigDecimal toBigDecimal() {
        return new BigDecimal( value );
    }

    @Override
	public int intValue() {
        return value;
    }

    @Override
	public long longValue() {
        return value;
    }

	@Override
	public String toString() {
		StringBuffer strBuffTab = new StringBuffer();
		for (int i = 0; i < getLevel(); i++) {
			strBuffTab.append("  ");
		}
		return strBuffTab.toString() + "Integer8=" + getValue() + "\n";
	}

}
