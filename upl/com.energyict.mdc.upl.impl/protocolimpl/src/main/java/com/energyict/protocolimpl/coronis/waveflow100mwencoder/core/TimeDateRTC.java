package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import java.io.IOException;
import java.util.Calendar;

import com.energyict.protocolimpl.modbus.enerdis.enerium200.parsers.TimeDateParser;

public class TimeDateRTC extends AbstractParameter {

	Calendar calendar;
	
	final Calendar getCalendar() {
		return calendar;
	}


	final void setCalendar(Calendar calendar) {
		this.calendar = calendar;
	}

	TimeDateRTC(WaveFlow100mW waveFlow100mW) {
		super(waveFlow100mW);
	}

	
	@Override
	ParameterId getParameterId() {
		return ParameterId.CurrentRTC;
	}

	@Override
	void parse(byte[] data) throws IOException {
		calendar = TimeDateRTCParser.parse(data, getWaveFlow100mW().getTimeZone());
	}

	@Override
	byte[] prepare() throws IOException {
		return TimeDateRTCParser.prepare(calendar);
	}

}
