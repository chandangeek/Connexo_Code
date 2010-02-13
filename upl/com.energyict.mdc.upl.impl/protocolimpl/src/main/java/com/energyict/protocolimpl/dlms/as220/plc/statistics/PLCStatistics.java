package com.energyict.protocolimpl.dlms.as220.plc.statistics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import com.energyict.dlms.axrdencoding.Array;
import com.energyict.protocol.IntervalData;

public class PLCStatistics extends Array {

	private final TimeZone timeZone;

	public PLCStatistics(byte[] profile, TimeZone timeZone) throws IOException {
		super(profile, 0, 0);
		this.timeZone = timeZone;
	}

	public TimeZone getTimeZone() {
		return timeZone;
	}

	public List<IntervalData> getProfileData() throws IOException {
		List<IntervalData> intervals = new ArrayList<IntervalData>();

		for (int i = 0; i < nrOfDataTypes(); i++) {
			StatisticsInterval interval = new StatisticsInterval(getDataType(i).getBEREncodedByteArray(), getTimeZone());
			System.out.println(interval);
		}

		return intervals;
	}

	@Override
	public String toString() {
		final String crlf = "\r\n";

		StringBuilder sb = new StringBuilder();

		try {
			getProfileData();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return sb.toString();
	}

}
