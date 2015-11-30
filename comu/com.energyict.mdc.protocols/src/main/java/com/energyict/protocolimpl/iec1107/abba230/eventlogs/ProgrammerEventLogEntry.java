package com.energyict.protocolimpl.iec1107.abba230.eventlogs;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;
import java.util.Date;
import java.util.TimeZone;

public class ProgrammerEventLogEntry {

	private Date timeStampIndex;
	private int infoIndex;
	private String programmerIndex;

	public ProgrammerEventLogEntry(byte[] data, int offset, TimeZone timeZone) throws IOException {
		long shift = (long)ProtocolUtils.getInt(data,offset,4)&0xFFFFFFFFL; offset+=4;
		if (shift>0) {
			timeStampIndex = ProtocolUtils.getCalendar(timeZone,shift).getTime();
			infoIndex = ProtocolUtils.getInt(data,offset++,1);
			programmerIndex = new String(ProtocolUtils.getSubArray2(data, offset, 12));
		}
	}

    public String toString() {
	    return "ProgrammerEventLogEntry:\n" +
			    "   infoIndex=" + getInfoIndex() + "\n" +
			    "   programmerIndex=" + getProgrammerIndex() + "\n" +
			    "   timeStampIndex=" + getTimeStampIndex() + "\n";
    }

	public static int size() {
	   return 17;
	}

	public Date getTimeStampIndex() {
		return timeStampIndex;
	}

	public int getInfoIndex() {
		return infoIndex;
	}

	public String getProgrammerIndex() {
		return programmerIndex;
	}
}
