package com.energyict.protocolimpl.iec1107.abba230.eventlogs;

import com.energyict.mdc.upl.ProtocolException;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.util.Date;
import java.util.TimeZone;

public class ProgrammerEventLogEntry {

	private Date timeStampIndex;
	private int infoIndex;
	private String programmerIndex;

	public ProgrammerEventLogEntry(byte[] data, int offset, TimeZone timeZone) throws ProtocolException {
		long shift = (long)ProtocolUtils.getInt(data,offset,4)&0xFFFFFFFFL; offset+=4;
		if (shift>0) {
			timeStampIndex = ProtocolUtils.getCalendar(timeZone,shift).getTime();
			infoIndex = ProtocolUtils.getInt(data,offset++,1);
			programmerIndex = new String(ProtocolUtils.getSubArray2(data, offset, 12));
		}
	}

//    public ProgrammerEventLogEntry() {
//    }
//    public static void main(String[] args) {
//        System.out.println(com.energyict.protocolimpl.base.ToStringBuilder.genCode(new ProgrammerEventLogEntry()));
//    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("ProgrammerEventLogEntry:\n");
        strBuff.append("   infoIndex="+getInfoIndex()+"\n");
        strBuff.append("   programmerIndex="+getProgrammerIndex()+"\n");
        strBuff.append("   timeStampIndex="+getTimeStampIndex()+"\n");
        return strBuff.toString();
    }

	static public int size() {
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
