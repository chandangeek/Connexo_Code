package com.energyict.protocolimpl.iec1107.abba230.eventlogs;

import java.io.IOException;
import java.util.*;

import com.energyict.protocol.*;

public class EndOfBillingEventLog extends AbstractEventLog {

	int mostRecent;
	int count;
	TimeStampInfoPair[] timeStampInfoPairs = new TimeStampInfoPair[10];
	
	public EndOfBillingEventLog(TimeZone timeZone) throws IOException {
		super(timeZone);
	}
	
	public void parse(byte[] data) throws IOException {
		int offset=0;
		mostRecent = ProtocolUtils.getIntLE(data, offset++, 1);
		count = ProtocolUtils.getIntLE(data, offset, 2); offset+=2;
        for( int i = 0; i < 10; i ++ ) {
    		timeStampInfoPairs[i] = new TimeStampInfoPair(data,offset,timeZone);
    		offset+=TimeStampInfoPair.size();
    		if (timeStampInfoPairs[i].getDate() != null)
    			addMeterEvent(new MeterEvent(timeStampInfoPairs[i].getDate(), MeterEvent.BILLING_ACTION,(timeStampInfoPairs[i].getInfoIndex()==1?"failed":"success")+" ("+count+")"));
        }

	}

}
