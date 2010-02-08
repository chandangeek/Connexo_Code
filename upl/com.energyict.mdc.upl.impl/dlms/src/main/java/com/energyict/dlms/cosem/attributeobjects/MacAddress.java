package com.energyict.dlms.cosem.attributeobjects;

import java.io.IOException;

import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.protocol.ProtocolUtils;

/**
 * @author jme
 *
 */
public class MacAddress extends Unsigned16 {

	/**
	 * @param berEncodedData
	 * @param offset
	 * @throws IOException
	 */
	public MacAddress(byte[] berEncodedData, int offset) throws IOException {
		super(berEncodedData, offset);
	}

	/**
	 * @param value
	 */
	public MacAddress(int value) {
		super(value);
	}

	@Override
	public String toString() {
		byte[] mac = new byte[2];
		mac[0] = (byte) ((getValue() >> 8) & 0x0FF);
		mac[1] = (byte) (getValue() & 0x0FF);
		return ProtocolUtils.getResponseData(mac);
	}

}
