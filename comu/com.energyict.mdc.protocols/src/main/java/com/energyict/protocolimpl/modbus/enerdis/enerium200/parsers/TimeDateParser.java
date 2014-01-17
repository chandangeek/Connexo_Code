package com.energyict.protocolimpl.modbus.enerdis.enerium200.parsers;

import com.energyict.mdc.protocol.api.ProtocolException;
import com.energyict.protocols.util.ProtocolUtils;
import com.energyict.protocolimpl.modbus.core.AbstractRegister;
import com.energyict.protocolimpl.modbus.core.Parser;
import com.energyict.protocolimpl.modbus.enerdis.enerium200.core.Utils;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class TimeDateParser  implements Parser {

	private static final int DEBUG 	= 0;
	private TimeZone timeZone 		= null;

	public static final String PARSER_NAME = "TimeDateParser";

	public TimeDateParser(TimeZone timeZone) {
		this.timeZone = timeZone;
	}

	public Object val(int[] values, AbstractRegister register) throws IOException {
		byte[] rawData = Utils.intArrayToByteArray(values);

		if (rawData.length != 4) {
			throw new ProtocolException(
					" Error in DateTimeParser. Wrong data length: " + rawData.length +
					" Data = " + ProtocolUtils.getResponseData(rawData)
			);
		}
		return parseTime(rawData);
	}

	public Date parseTime(byte[] rawData) {
		Calendar cal = ProtocolUtils.getCleanCalendar(timeZone);
		long secondsSince1970GMT = ProtocolUtils.getInt(rawData);
		cal.setTime(new Date(secondsSince1970GMT * 1000));
		cal.add(Calendar.HOUR, -1);
		return cal.getTime();
	}

	public void setTimeZone(TimeZone timeZone) {
		this.timeZone = timeZone;
	}

	public static byte[] getBytesFromDate(Date date) {
		long secondsSince1970GMT = (date.getTime() / 1000) + 3600;
		byte[] returnValue = ProtocolUtils.getSubArray2(Utils.longToBytes(secondsSince1970GMT), 4, 4);
		return returnValue;
	}

}
