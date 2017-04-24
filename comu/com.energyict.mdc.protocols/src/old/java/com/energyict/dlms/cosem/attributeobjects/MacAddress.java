/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.cosem.attributeobjects;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.dlms.axrdencoding.Unsigned16;

import java.io.IOException;

/**
 * @author jme
 *
 */
public class MacAddress extends Unsigned16 {

	private static final int	BYTE_LEN	= 8;

	/**
	 * @param berEncodedData
	 * @param offset
	 * @throws java.io.IOException
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
		mac[0] = (byte) ((getValue() >> BYTE_LEN) & 0x0FF);
		mac[1] = (byte) (getValue() & 0x0FF);
		return ProtocolUtils.getResponseData(mac);
	}

}
