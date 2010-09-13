package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import java.io.IOException;
import java.util.Calendar;

import com.energyict.protocol.*;

public class BatteryLifeDateEnd extends AbstractParameter {

	private Calendar calendar;

	
	final Calendar getCalendar() {
		return calendar;
	}

	BatteryLifeDateEnd(WaveFlow100mW waveFlow100mW) {
		super(waveFlow100mW);
	}

	
	@Override
	ParameterId getParameterId() {
		return ParameterId.BatteryLifeDateEnd;
	}

	@Override
	void parse(byte[] data) throws IOException {
		
		long date = ProtocolUtils.getLong(data, 0, 7);
		if (date == 0x01010101010101L) {
			calendar = null;
		}
		else {
			calendar = TimeDateRTCParser.parse(data, getWaveFlow100mW().getTimeZone());
		}
	}

	@Override
	byte[] prepare() throws IOException {
		throw new UnsupportedException();
	}
	

}
