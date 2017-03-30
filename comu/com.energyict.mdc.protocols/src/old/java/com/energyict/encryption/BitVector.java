/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.encryption;

import com.energyict.protocols.util.ProtocolUtils;

public class BitVector {
	private byte[] value;

	public static BitVector concatenate(BitVector v1, BitVector v2) {
		BitVector result = new BitVector(v1.length() + v2.length());
		for (int i = 0; i < v1.length(); i++) {
			result.setValue(v1.getValue(i), i);
		}
		for (int j = 0; j < v2.length(); j++) {
			result.setValue(v2.getValue(j), j + v1.length());
		}
		return result;
	}

	public static BitVector leftShift(BitVector v1) {
		BitVector result = new BitVector(v1);
		result.leftshift();
		return result;
	}

	public static BitVector addition(BitVector t1, BitVector t2) {
		BitVector result = new BitVector(t1.length());
		for (int i = 0; i < t1.length(); i++) {
			result.setValue((byte) ((t1.getValue(i) ^ t2.getValue(i)) & 0xFF),
					i);
		}
		return result;
	}

	public BitVector convertEndianess() {
		BitVector result = new BitVector(this.length());
		for (int i = 0; i < this.length(); i++) {
			result
					.setValue((byte) convertEndiannessOfByte(this.getValue(i)),
							i);
		}
		return result;
	}

	public static BitVector multiplication(BitVector f1, BitVector f2) {
		BitVector z = new BitVector(16);
		BitVector v = f1.convertEndianess();
		BitVector r = new BitVector("87000000000000000000000000000000");
		BitVector y = f2.convertEndianess();

		for (int i = 0; i < 128; i++) {
			if (y.isBitSet(i)) {
				z = BitVector.addition(z, v);
			}
			if (!v.isBitSet(127)) {
				v = BitVector.leftShift(v);
			} else {
				v = BitVector.addition(BitVector.leftShift(v), r);
			}
		}
		return z.convertEndianess();
	}

	public static BitVector convertFromInt(int value, int length) {
		BitVector result = new BitVector(length);
		result.setValue((byte) (value & 0x000000FF), length - 1);
		result.setValue((byte) ((value & 0x0000FF00) / (0x100)), length - 2);
		result.setValue((byte) ((value & 0x00FF0000) / (0x10000)), length - 3);
		result
				.setValue((byte) ((value & 0xFF000000) / (0x1000000)),
						length - 4);
		return result;

	}

	public BitVector(int length) {
		value = new byte[length];
		for (int i = 0; i < length; i++) {
			value[i] = 0;
		}
	}

	public BitVector(byte[] newValue) {
		value = newValue;
	}

	public BitVector(BitVector newValue) {
		value = new byte[newValue.length()];
		for (int i = 0; i < newValue.length(); i++) {
			value[i] = newValue.getValue(i);
		}

	}

	public byte convertEndiannessOfByte(byte value) {
		byte[] nibble = new byte[16];
		nibble[0] = 0;
		nibble[1] = 8;
		nibble[2] = 4;
		nibble[3] = 12;
		nibble[4] = 2;
		nibble[5] = 10;
		nibble[6] = 6;
		nibble[7] = 14;
		nibble[8] = 1;
		nibble[9] = 9;
		nibble[10] = 5;
		nibble[11] = 13;
		nibble[12] = 3;
		nibble[13] = 11;
		nibble[14] = 7;
		nibble[15] = 15;

		byte result = (byte) ((nibble[value & 0x0F] << 4) & 0xFF);
		result = (byte) (result | nibble[(value & 0xFF) >> 4]);
		return result;
	}

	public BitVector(String asHex) {
		value = new byte[asHex.length() / 2];
		for (int i = 0; i < value.length; i++) {
			value[i] = (byte) (Integer.valueOf(asHex
					.substring(i * 2, i * 2 + 2), 16) & 0xFF);
		}
	}

	public int length() {
		return value.length;
	}

	public byte getValue(int index) {
		return value[index];
	}

	public byte[] getValue() {
		return value;
	}

	public BitVector get128Segment(int index) {
		BitVector result = new BitVector(16);
		for (int j = 0; j < 16; j++) {
			int valueIndex = index * 16 + j;
			if (valueIndex >= value.length)
				result.setValue((byte) 0, j);
			else
				result.setValue(value[index * 16 + j], j);
		}
		return result;
	}

	public void set128Segment(int index, BitVector seg) {

		for (int j = 0; j < 16; j++) {
			if (index * 16 + j < value.length)
				value[index * 16 + j] = seg.getValue(j);
			else
				break;
		}
	}

	public void setValue(byte value, int index) {
		this.value[index] = value;
	}

	public void setValue(byte[] value) {
		this.value = value;
	}

	/**
	 * checks is a bit is set, bits are numbered from 0 onwards, bit 0 being the
	 * LSB
	 *
	 * @return
	 */
	public boolean isBitSet(int bitnumber) {
		int index = bitnumber / 8;
		int bits = 1 << (bitnumber % 8);

		return (value[index] & bits) != 0;
	}

	private void leftshift() {
		for (int i = this.length() - 1; i >= 0; i--) {
			value[i] = (byte) ((value[i] << 1) & 0xFF);
			if ((i > 0) && ((value[i - 1] & 0x80) != 0)) {
				value[i] = (byte) ((value[i] | 0x01) & 0xFF);
			}
		}
	}

	public String toString() {
		String result = new String();
		for (int i = 0; i < value.length; i++) {
			byte token = (byte) (value[i] & 0xFF);
			if ((token & 0xF0) == 0) {
				result += "0";
			}
			result += Integer.toHexString(token & 0xFF);
		}
		return result;
	}

	/**
	 * @deprecated - It seems that this one gets the LSB's instead of the MSB's
	 */
	public BitVector Msb(int n) {
		BitVector result = new BitVector(n);
		for (int i = this.length() - n; i < this.length(); i++) {
			result.setValue(this.getValue(i), i - (this.length() - n));
		}
		return result;
	}

	public BitVector Msb2(int n){
		BitVector result = new BitVector(n);
		result.setValue(ProtocolUtils.getSubArray2(this.getValue(), 0, n));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof BitVector)) {
			return false;
		}
		BitVector v = (BitVector) obj;
		if (v.length() != this.length()) {
			return false;
		}
		for (int i = 0; i < this.length(); i++) {
			if (this.value[i] != v.getValue(i)) {
				return false;
			}
		}
		return true;
	}

}
