package com.energyict.protocolimpl.dlms.as220.plc;

import java.io.IOException;
import java.util.TimeZone;

import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.util.DateTime;

public class PLCStatistics extends Array {

	private final TimeZone timeZone;

	public PLCStatistics(byte[] profile, TimeZone timeZone) throws IOException {
		super(profile, 0, 0);
		this.timeZone = timeZone;
	}

	public TimeZone getTz() {
		return timeZone;
	}

	@Override
	public String toString() {
		final String crlf = "\r\n";

		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < nrOfDataTypes(); i++) {
			DateTime timeStamp = getDataType(i).getStructure().getDataType(0).getOctetString().getDateTime(timeZone);
			sb.append(timeStamp).append(crlf);
		}

		return sb.toString();
	}

}
