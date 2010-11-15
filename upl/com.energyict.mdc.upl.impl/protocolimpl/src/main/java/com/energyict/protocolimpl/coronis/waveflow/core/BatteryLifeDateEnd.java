package com.energyict.protocolimpl.coronis.waveflow.core;

import java.io.IOException;
import java.util.Calendar;

import com.energyict.protocol.*;

public class BatteryLifeDateEnd extends AbstractParameter {

	private Calendar calendar;

	
	final Calendar getCalendar() {
		return calendar;
	}

	BatteryLifeDateEnd(WaveFlow waveFlow) {
		super(waveFlow);
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
			calendar = TimeDateRTCParser.parse(data, getWaveFlow().getTimeZone());
		}
	}

	@Override
	byte[] prepare() throws IOException {
		throw new UnsupportedException();
	}
	

}
