package com.energyict.dlms.cosem.attributeobjects;

import java.io.IOException;

import com.energyict.dlms.axrdencoding.Structure;

/**
 * @author jme
 *
 */
public class MacUnsigned32Couple extends Structure {

	public MacUnsigned32Couple(byte[] berEncodedData) throws IOException {
		super(berEncodedData, 0, 0);
	}

	public MacAddress getMacAddress() {
		return new MacAddress(getDataType(0).getUnsigned16().getValue());
	}

	public long getCounter() {
		return getDataType(1).getUnsigned32().getValue();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("macAddr=").append(getMacAddress()).append(", ");
		sb.append("counter=").append(getCounter());
		return sb.toString();
	}

}
