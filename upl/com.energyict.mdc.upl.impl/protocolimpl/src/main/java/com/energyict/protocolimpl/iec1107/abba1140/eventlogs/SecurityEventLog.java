package com.energyict.protocolimpl.iec1107.abba1140.eventlogs;

import com.energyict.mdc.upl.ProtocolException;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.util.TimeZone;

public class SecurityEventLog extends AbstractEventLog {

	static final String EVENT_NAME 		= "Configuration change user: ";
	static final int EVENT_CODE 		= MeterEvent.CONFIGURATIONCHANGE;

	static final int PR_SRC_OFFSET      = 14;
	static final int PR_SRC_LENGTH      = 1;

	static final int USERNAME_OFFSET      = 17;
	static final int USERNAME_LENGTH      = 12;

	//	•	First 2 bytes is cumulative count(how many times has the meter been programmed).These bytes are reversed(little Endian) so 0E00 represent 14 times (000E).
	//	•	Next 12 bytes represent time -stamp for the last 3 programing event, each programming event being 4 bytes long.Again , these bytes are “reversed “representing seconds since 01-01-1970 00:00:00.
	//	•	Next 3 bytes represent the source, i.e., how was programming done. Was it locally or remotely? Each byte represent source for each programming event ,00 =>Optical Port(Local) and 01=> Remote Comms
	//	•	Last 36 bytes represent Username, i.e., the profile or user who programmed the meter. Each Username is a 12 byte string.
	//	Totals bytes are 53 (35 HEX).
	public void parse(byte[] data) throws ProtocolException {
		count = ProtocolUtils.getIntLE(data, 0, COUNT_SIZE);
		for( int i = 0; i < NUMBER_OF_EVENTS; i++ ) {
			timeStamp[i] = new TimeStamp(data, COUNT_SIZE + (i*TIMESTAMP_SIZE), getTimeZone());
			int prSrc = ProtocolUtils.getIntLE(data, PR_SRC_OFFSET + i, PR_SRC_LENGTH );
			String userName = new String( ProtocolUtils.getSubArray2( data, USERNAME_OFFSET + (USERNAME_LENGTH * i), USERNAME_LENGTH )  ).trim();
			String dscr = EVENT_NAME + userName + ((prSrc==0) ?", optical communications port" :", remote communications port");
			if (timeStamp[i].getTimeStamp() != null)
				addMeterEvent(new MeterEvent(timeStamp[i].getTimeStamp(), getEventCode(), dscr));
		}
		debug();
	}

	public SecurityEventLog(TimeZone timeZone) {
		super(timeZone);
	}

	protected int getEventCode() {
		return EVENT_CODE;
	}

	protected String getEventName() {
		return EVENT_NAME;
	}

}
