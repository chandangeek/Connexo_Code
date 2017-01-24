package com.energyict.protocolimpl.dlms.as220.plc.statistics;

import com.energyict.dlms.axrdencoding.Structure;

import java.io.IOException;

/**
 * @author jme
 *
 */
public class PlcSNR extends Structure {

	public PlcSNR(byte[] berEncodedByteArray) throws IOException {
		super(berEncodedByteArray, 0, 0);
	}

	public int getChannelNr() {
		return getDataType(0).getUnsigned8().getValue();
	}

	public int getS0() {
		return getDataType(1).getUnsigned16().getValue();
	}

	public int getN0() {
		return getDataType(2).getUnsigned16().getValue();
	}

	public int getS1() {
		return getDataType(3).getUnsigned16().getValue();
	}

	public int getN1() {
		return getDataType(4).getUnsigned16().getValue();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("CH=").append(getChannelNr()).append(", ");
		sb.append("S0=").append(getS0()).append(", ");
		sb.append("N0=").append(getN0()).append(", ");
		sb.append("S1=").append(getS1()).append(", ");
		sb.append("N1=").append(getN1());
		return sb.toString();
	}

}
