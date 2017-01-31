/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import com.energyict.protocolimpl.coronis.core.TimeDateRTCParser;

import java.io.IOException;
import java.util.Calendar;

public class TimeDateRTC extends AbstractParameter {

	private Calendar calendar;
	
	final Calendar getCalendar() {
		return calendar;
	}


	final void setCalendar(final Calendar calendar) {
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
