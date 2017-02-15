/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.axrdencoding;

import com.energyict.mdc.protocol.api.ProtocolException;

import java.io.IOException;
import java.math.BigDecimal;

public class BooleanObject extends AbstractDataType {

	private static final int SIZE = 2;
    private int trueValue = 0xFF;
    private static final int FALSE = 0x00;
	private boolean state;

	public BooleanObject(boolean state) {
		this.state = state;
	}

    /**
     * The AXDR spec explains that all values different from 0x00 are accepted as boolean TRUE.
     * However, some meters don't follow this spec and require a specific value, e.g. 0x01.
     */
    public void setTrueValue(int trueValue) {
        this.trueValue = trueValue;
    }

    /**
     * Creates a new instance of Enum
     */
	public BooleanObject(byte[] berEncodedData, int offset) throws IOException {
		if (berEncodedData[offset] != AxdrType.BOOLEAN.getTag()) {
			throw new ProtocolException("BooleanObject, invalid identifier " + berEncodedData[offset]);
		}
		offset++;
        setState(berEncodedData[offset] != FALSE);
	}

	public String toString() {
        StringBuilder strBuffTab = new StringBuilder();
		for (int i = 0; i < getLevel(); i++) {
			strBuffTab.append("  ");
		}
		return strBuffTab.toString() + "BooleanObject = " + getState() + "\n";
	}

	public boolean getState() {
		return this.state;
	}

	private void setState(boolean state) {
		this.state = state;
	}

	protected byte[] doGetBEREncodedByteArray() {
		byte[] data = new byte[2];
		data[0] = AxdrType.BOOLEAN.getTag();
        data[1] = (byte) (state ? trueValue : FALSE);
		return data;
	}

	public int intValue() {
		return state ? 1 : 0;
	}

	public long longValue() {
		return state ? 1 : 0;
	}

	protected int size() {
		return SIZE;
	}

	public BigDecimal toBigDecimal() {
		return new BigDecimal(intValue());
	}

}
