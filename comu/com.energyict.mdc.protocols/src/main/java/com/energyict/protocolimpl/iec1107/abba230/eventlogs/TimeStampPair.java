package com.energyict.protocolimpl.iec1107.abba230.eventlogs;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;
import java.util.Date;
import java.util.TimeZone;

public class TimeStampPair {

    private Date startDate;
    private Date endDate;

    public TimeStampPair(byte[] data, int offset, TimeZone timeZone) throws IOException {
		long shift = (long)ProtocolUtils.getInt(data,offset,4)&0xFFFFFFFFL; offset+=4;
		if (shift>0) {
			startDate = ProtocolUtils.getCalendar(timeZone,shift).getTime();
			shift = (long)ProtocolUtils.getInt(data,offset,4)&0xFFFFFFFFL; offset+=4;
			endDate = ProtocolUtils.getCalendar(timeZone,shift).getTime();
		}
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("TimeStampPair:\n");
        strBuff.append("   endDate="+getEndDate()+"\n");
        strBuff.append("   startDate="+getStartDate()+"\n");
        return strBuff.toString();
    }

    static public int size() {
    	return 8;
    }

	public Date getStartDate() {
		return startDate;
	}

	public Date getEndDate() {
		return endDate;
	}
}
