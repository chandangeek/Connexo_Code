package com.energyict.protocolimpl.cm10;

import java.io.IOException;

import com.energyict.protocol.ProtocolUtils;

public class CurrentDialReadingsTable {
	
	private CM10 cm10Protocol;
	private long dialReadings[] = new long[48];
	
	public CurrentDialReadingsTable(CM10 cm10Protocol) {
		this.cm10Protocol = cm10Protocol;
	}
	
	public void parse(byte[] data) throws IOException {
		for (int i = 0; i < 48; i++) {
			dialReadings[i] = ProtocolUtils.getLongLE(data, i * 3, 3);
		}
	}
	
	public String toString() {
		StringBuffer buf = new StringBuffer("");
		for (int i = 0; i < 48; i++)
			buf.append("current dial reading " + i + ": " + dialReadings[i]).append("\n");
		return buf.toString();
	}

}
