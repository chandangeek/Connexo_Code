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

import com.energyict.mdc.upl.ProtocolException;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.math.BigDecimal;

/**
 *
 * @author kvds
 */
public class Integer16 extends AbstractDataType {

	private short value;

	/** Creates a new instance of Enum */
	public Integer16(byte[] berEncodedData, int offset) throws IOException {
		if (berEncodedData[offset] != AxdrType.LONG.getTag()) {
			throw new ProtocolException("Integer16, invalid identifier " + berEncodedData[offset]);
		}
		offset++;
		value = (short) ProtocolUtils.getInt(berEncodedData, offset, 2);
		offset += 2;
	}

	public Integer16(int value) {
		this.value = (short) value;
	}

	public Integer16(short value) {
		this.value = value;
	}

	protected byte[] doGetBEREncodedByteArray() {
		byte[] data = new byte[3];
		data[0] = AxdrType.LONG.getTag();
		data[1] = (byte) ((value & 0xffff) / 256);
		data[2] = (byte) ((value & 0xffff) % 256);
		return data;
	}

	protected int size() {
		return 3;
	}

	public int getValue() {
		return value;
	}

	public void setValue(short value) {
		this.value = value;
	}

	public BigDecimal toBigDecimal() {
		return new BigDecimal(value);
	}

	public int intValue() {
		return value;
	}

	public long longValue() {
		return value;
	}

	public String toString() {
		StringBuffer strBuffTab = new StringBuffer();
		for (int i = 0; i < getLevel(); i++) {
			strBuffTab.append("  ");
		}
		return strBuffTab.toString() + "Integer16=" + getValue() + "\n";
	}

}
