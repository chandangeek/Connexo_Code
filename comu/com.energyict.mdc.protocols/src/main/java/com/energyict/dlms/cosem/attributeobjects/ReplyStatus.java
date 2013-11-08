package com.energyict.dlms.cosem.attributeobjects;

import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned8;

import java.io.IOException;

/**
 * @author jme
 *
 */
public class ReplyStatus extends Structure {

	public ReplyStatus(byte[] berEncodedByteArray, int offset, int level) throws IOException {
		super(berEncodedByteArray, offset, level);
	}

	public int getLSAPSelector() {
		Unsigned8 value = getDataType(0).getUnsigned8();
		return value != null ? value.getValue() : 0;
	}

	public int getLengthOfWaitingLSDU() {
		Unsigned8 value = getDataType(1).getUnsigned8();
		return value != null ? value.getValue() : 0;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("LSAPSelector=").append(getLSAPSelector()).append(", ");
		sb.append("LengthOfWaitingLSDU=").append(getLengthOfWaitingLSDU());
		return sb.toString();
	}

}
