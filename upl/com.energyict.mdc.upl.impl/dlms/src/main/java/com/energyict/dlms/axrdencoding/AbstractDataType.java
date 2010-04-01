/*
 * AbstractDataType.java
 * Created on 17 oktober 2007, 14:19
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.dlms.axrdencoding;

import java.math.BigDecimal;

import com.energyict.protocolimpl.utils.ProtocolTools;

/**
 * @author kvds
 */
abstract public class AbstractDataType {

	private int	level;

	protected abstract byte[] doGetBEREncodedByteArray();

	protected abstract int size();
	public abstract int intValue();
	public abstract BigDecimal toBigDecimal();
	public abstract long longValue();

	public Array getArray() {
		return isArray() ? (Array) this : null;
	}

	public byte[] getBEREncodedByteArray() {
		return doGetBEREncodedByteArray();
	}

	public byte[] getContentByteArray() {
		byte[] berEncoded = getBEREncodedByteArray();
		return ProtocolTools.getSubArray(berEncoded, 1);
	}

	public BitString getBitString() {
		return isBitString() ? (BitString) this : null;
	}

	/**
	 * Size of the complete abstractDatatype inclusive the length (if applicable) and tag.
	 * @return
	 */
	public int getDecodedSize() {
		return size();
	}

	public Integer16 getInteger16() {
		return isInteger16() ? (Integer16) this : null;
	}

	public Integer32 getInteger32() {
		return isInteger32() ? (Integer32) this : null;
	}

	public Integer64 getInteger64() {
		return isInteger64() ? (Integer64) this : null;
	}

	public Integer8 getInteger8() {
		return isInteger8() ? (Integer8) this : null;
	}

	public int getLevel() {
		return level;
	}

	public NullData getNullData() {
		return isNullData() ? (NullData) this : null;
	}

	public OctetString getOctetString() {
		return isOctetString() ? (OctetString) this : null;
	}

	public Structure getStructure() {
		return isStructure() ? (Structure) this : null;
	}

	public TypeEnum getTypeEnum() {
		return isTypeEnum() ? (TypeEnum) this : null;
	}

	public Unsigned16 getUnsigned16() {
		return isUnsigned16() ? (Unsigned16) this : null;
	}

	public Unsigned32 getUnsigned32() {
		return isUnsigned32() ? (Unsigned32) this : null;
	}

	public Unsigned8 getUnsigned8() {
		return isUnsigned8() ? (Unsigned8) this : null;
	}

	public VisibleString getVisibleString() {
		return isVisibleString() ? (VisibleString) this : null;
	}

	public boolean isArray() {
		return this instanceof Array;
	}

	public boolean isBitString() {
		return this instanceof BitString;
	}

	public boolean isInteger16() {
		return this instanceof Integer16;
	}

	public boolean isInteger32() {
		return this instanceof Integer32;
	}

	public boolean isInteger64() {
		return this instanceof Integer64;
	}

	public boolean isInteger8() {
		return this instanceof Integer8;
	}

	public boolean isNullData() {
		return this instanceof NullData;
	}

	public boolean isOctetString() {
		return this instanceof OctetString;
	}

	public boolean isStructure() {
		return this instanceof Structure;
	}

	public boolean isTypeEnum() {
		return this instanceof TypeEnum;
	}

	public boolean isUnsigned16() {
		return this instanceof Unsigned16;
	}

	public boolean isUnsigned32() {
		return this instanceof Unsigned32;
	}

	public boolean isUnsigned8() {
		return this instanceof Unsigned8;
	}

	public boolean isVisibleString() {
		return this instanceof VisibleString;
	}

	public void setLevel(int level) {
		this.level = level;
	}

}
