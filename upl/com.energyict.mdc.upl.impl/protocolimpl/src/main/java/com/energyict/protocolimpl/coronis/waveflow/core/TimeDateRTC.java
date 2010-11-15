package com.energyict.protocolimpl.coronis.waveflow.core;

import java.io.IOException;
import java.util.Calendar;

import com.energyict.protocolimpl.modbus.enerdis.enerium200.parsers.TimeDateParser;

public class TimeDateRTC extends AbstractParameter {

	private Calendar calendar;
	
	final Calendar getCalendar() {
		return calendar;
	}


	final void setCalendar(final Calendar calendar) {
		this.calendar = calendar;
	}

	TimeDateRTC(WaveFlow waveFlow) {
		super(waveFlow);
	}

	
	@Override
	ParameterId getParameterId() {
		return ParameterId.CurrentRTC;
	}

	@Override
	void parse(byte[] data) throws IOException {
		calendar = TimeDateRTCParser.parse(data, getWaveFlow().getTimeZone());
	}

	@Override
	byte[] prepare() throws IOException {
		return TimeDateRTCParser.prepare(calendar);
	}

}
