/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 *
 */
package com.energyict.dlms.cosem.attributeobjects;

import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned32;

import java.io.IOException;

/**
 * @author jme
 *
 */
public class DesynchronizationListing extends Structure {

	private static final int	PHYSICAL_LAYER	= 0;
	private static final int	NOT_ADDRESSED	= 1;
	private static final int	CRC_NOT_OK		= 2;
	private static final int	WRITE_REQUEST	= 3;
	private static final int	WRONG_INIT		= 4;

	public DesynchronizationListing(byte[] berEncodedData, int offset, int level) throws IOException {
		super(berEncodedData, offset, level);
	}

	public long getPhysicalLayer(){
		Unsigned32 value = getDataType(PHYSICAL_LAYER).getUnsigned32();
		return value != null ? value.getValue() : 0;
	}

	public long getTimeoutNotAddressed() {
		Unsigned32 value = getDataType(NOT_ADDRESSED).getUnsigned32();
		return value != null ? value.getValue() : 0;
	}

	public long getTimeoutCrcNotOk() {
		Unsigned32 value = getDataType(CRC_NOT_OK).getUnsigned32();
		return value != null ? value.getValue() : 0;
	}

	public long getWriteRequest() {
		Unsigned32 value = getDataType(WRITE_REQUEST).getUnsigned32();
		return value != null ? value.getValue() : 0;
	}

	public long getwrongInitiator() {
		Unsigned32 value = getDataType(WRONG_INIT).getUnsigned32();
		return value != null ? value.getValue() : 0;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("PhysicalLayer=").append(getPhysicalLayer()).append(", ");
		sb.append("TimeoutNotAddr=").append(getTimeoutNotAddressed()).append(", ");
		sb.append("TimeoutCrcNotOk=").append(getTimeoutCrcNotOk()).append(", ");
		sb.append("WriteReq=").append(getWriteRequest()).append(", ");
		sb.append("wrongInit=").append(getwrongInitiator());
		return sb.toString();
	}

}
