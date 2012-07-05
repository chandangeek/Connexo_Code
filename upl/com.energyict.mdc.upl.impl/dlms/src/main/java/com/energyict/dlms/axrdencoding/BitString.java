/*
 * BitString.java
 *
 * Created on 16 oktober 2007, 11:35
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.dlms.axrdencoding;

import com.energyict.dlms.DLMSUtils;

import java.io.IOException;
import java.math.BigDecimal;

/**
 *
 * @author kvds
 *
 * TODO -> WARNING: MAXIMUM LENGTH = 64 BITS
 * TODO -> IF BITSTRING HAS MORE BITS, AN OVERFLOW WILL OCCUR (AS VALUE IS OF TYPE LONG)!
 */
public class BitString extends AbstractDataType {

	private long value;
    private int size;       // The number of bits in the BIT string (without any trailing bits).
    private int nrOfBytes;  // The number of bytes needed to store the BIT string (BIT string bits + trailing bits)
    private int offsetBegin, offsetEnd;

	/** Creates a new instance of BitString */
	public BitString(byte[] berEncodedData, int offset) throws IOException {
		offsetBegin = offset;

		if (berEncodedData[offset] != AxdrType.BIT_STRING.getTag()) {
			throw new IOException("BitString, invalid identifier " + berEncodedData[offset]);
		}

		offset++;
		size = (int) DLMSUtils.getAXDRLength(berEncodedData, offset);
		offset += DLMSUtils.getAXDRLengthOffset(berEncodedData, offset);

		// calc nr of bytes
		if ((size % 8) == 0) {
			nrOfBytes = (size / 8);
		} else {
			nrOfBytes = ((size / 8) + 1);
		}

/*
        // Why can't size be > 8 bytes ??? Should be possible
		if (size > 8) {
			throw new IOException("BitString, invalid length " + size);
		}
*/

		value = 0;
		for (int i = 0; i < nrOfBytes; i++) {
            value += ((long) ((berEncodedData[offset + i] & 0xff)) << ((nrOfBytes - i -1) * 8));
		}

		offset += nrOfBytes;

		offsetEnd = offset;
	}

	public String toString() {

		StringBuffer strBuff = new StringBuffer();
		for (int i = 0; i < getLevel(); i++) {
			strBuff.append("  ");
		}
		return strBuff.toString() + "BitString=0x" + Long.toHexString(value) + "\n";
	}

	public BitString(long value) {
		this(value, 64);
	}

	public BitString(int value) {
		this(value, 32);
	}

	public BitString(long value, int size) {
		this.value = value;
		this.size = size;
	}

	protected byte[] doGetBEREncodedByteArray() {

		byte[] encodedLength = DLMSUtils.getAXDRLengthEncoding(size);
		byte[] data = new byte[(size / 8) + ((size % 8) > 0 ? 1 : 0) + 1 + encodedLength.length];
		data[0] = AxdrType.BIT_STRING.getTag();
		for (int i = 0; i < encodedLength.length; i++) {
			data[1 + i] = encodedLength[i];
		}
		for (int i = 0; i < (data.length - (1 + encodedLength.length)); i++) {
			data[(data.length - 1) - i] = (byte) (value >> (8 * i));
		}
		return data;
	}

	protected int size() {
		return offsetEnd - offsetBegin;
	}

	public BigDecimal toBigDecimal() {
		return new BigDecimal(value);
	}

	public int intValue() {
		return (int) (value);
	}

	public long longValue() {
		return value;
	}
}
