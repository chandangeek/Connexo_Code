package com.energyict.protocolimpl.iec1107.abba230_.eventlogs;

import java.io.IOException;
import java.util.*;

import com.energyict.protocol.ProtocolUtils;

public class TimeStampInfoPair {

    private Date date;
    private int infoIndex;
	
    public TimeStampInfoPair(byte[] data, int offset, TimeZone timeZone) throws IOException {
		long shift = (long)ProtocolUtils.getInt(data,offset,4)&0xFFFFFFFFL; offset+=4;
		if (shift > 0) {
			date = ProtocolUtils.getCalendar(timeZone,shift).getTime();
			infoIndex = ProtocolUtils.getInt(data,offset++,1);
		}
    }
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("TimeStampPair:\n");
        strBuff.append("   date="+getDate()+"\n");
        strBuff.append("   infoIndex="+getInfoIndex()+"\n");
        return strBuff.toString();
    }
    
    static public int size() {
    	return 5;
    }

	public Date getDate() {
		return date;
	}

	public int getInfoIndex() {
		return infoIndex;
	}


}
