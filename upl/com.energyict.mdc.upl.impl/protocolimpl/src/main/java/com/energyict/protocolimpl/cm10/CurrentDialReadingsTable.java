package com.energyict.protocolimpl.cm10;

import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

public class CurrentDialReadingsTable {
	
	private CM10 cm10Protocol;
	private long dialReadings[] = new long[48];
	private Calendar toTime;
	
	public CurrentDialReadingsTable(CM10 cm10Protocol) {
		this.cm10Protocol = cm10Protocol;
	}
	
	public void parse(byte[] data) throws IOException {
		toTime = Calendar.getInstance(cm10Protocol.getTimeZone());
		for (int i = 0; i < 48; i++) {
			dialReadings[i] = ProtocolUtils.getLongLE(data, i * 3, 3);
		}
	}
	
	public long[] getValues() {
		return dialReadings;
	}
	
	public Date getToTime() {
		return toTime.getTime();
	}
	
	public String toString() {
		StringBuffer buf = new StringBuffer("");
		for (int i = 0; i < 48; i++)
			buf.append("current dial reading " + i + ": " + dialReadings[i]).append("\n");
		return buf.toString();
	}

}
