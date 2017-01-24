package com.energyict.protocolimpl.coronis.waveflowDLMS;

import com.energyict.protocolimpl.coronis.core.ProtocolLink;
import com.energyict.protocolimpl.coronis.core.TimeDateRTCParser;

import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;

public class TimeDateRTC extends AbstractParameter {

	private Calendar calendar;
	
	final Calendar getCalendar() {
		return calendar;
	}


	final void setCalendar(final Calendar calendar) {
		this.calendar = calendar;
	}

	TimeDateRTC(ProtocolLink protocolLink) {
		super(protocolLink);
	}

	
	@Override
	ParameterId getParameterId() {
		return ParameterId.CurrentRTC;
	}

	@Override
	void parse(byte[] data) throws IOException {
		calendar = TimeDateRTCParser.parse(data, TimeZone.getTimeZone("GMT")); //getProtocolLink().getTimeZone());
	}

	@Override
	byte[] prepare() throws IOException {
		return TimeDateRTCParser.prepare(calendar);
	}

}
