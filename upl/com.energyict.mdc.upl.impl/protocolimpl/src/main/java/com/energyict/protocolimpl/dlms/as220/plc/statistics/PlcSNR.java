package com.energyict.protocolimpl.dlms.as220.plc.statistics;

import java.io.IOException;

import com.energyict.dlms.axrdencoding.Structure;

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

	public int getSnr0() {
		return getDataType(1).getUnsigned16().getValue();
	}

	public int getSnr1() {
		return getDataType(2).getUnsigned16().getValue();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("CH=").append(getChannelNr()).append(", ");
		sb.append("SNR0=").append(getSnr0()).append(", ");
		sb.append("SNR1=").append(getSnr1());
		return sb.toString();
	}

}
