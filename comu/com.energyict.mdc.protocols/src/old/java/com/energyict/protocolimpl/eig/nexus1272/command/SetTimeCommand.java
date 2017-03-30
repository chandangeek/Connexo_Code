/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.eig.nexus1272.command;

import java.util.Calendar;
import java.util.TimeZone;


public class SetTimeCommand extends AbstractWriteMultipleRegistersCommand {

	private TimeZone tz;

	public SetTimeCommand(int transID) {
		super(transID);
		startingAddress = new byte[] {(byte) 0x00, 0x54};
		numSetPoints = new byte[] {0x00, 0x05};
		payloadByteCount = 0x0A;
		length = 0x11;
	}
	
	public void setTimeZone(TimeZone tz) {
		this.tz = tz;
	}

	@Override
	protected byte[] getPayload() {
		Calendar cal = Calendar.getInstance();
		cal.setTimeZone(tz);
		byte century = (byte) (cal.get(Calendar.YEAR)/100);
		byte year = (byte) (cal.get(Calendar.YEAR)%100);
		byte month = (byte) (cal.get(Calendar.MONTH)+1);
		byte day = (byte) cal.get(Calendar.DAY_OF_MONTH);
		byte hour = (byte) cal.get(Calendar.HOUR_OF_DAY );
		byte minute = (byte) cal.get(Calendar.MINUTE);
		byte second = (byte) cal.get(Calendar.SECOND);
		byte tenMilli = (byte) (cal.get(Calendar.MILLISECOND)/10);
		byte dayOfWeek = (byte) cal.get(Calendar.DAY_OF_WEEK);
		 
		return new byte[] {century, year, month, day, hour, minute, second, tenMilli, 0x00, dayOfWeek};
	}

}
