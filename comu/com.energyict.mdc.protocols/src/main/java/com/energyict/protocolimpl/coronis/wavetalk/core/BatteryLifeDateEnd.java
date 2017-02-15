/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.wavetalk.core;

import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.coronis.core.TimeDateRTCParser;

import java.io.IOException;
import java.util.Calendar;

public class BatteryLifeDateEnd extends AbstractParameter {

	private Calendar calendar;


	final Calendar getCalendar() {
		return calendar;
	}

	BatteryLifeDateEnd(AbstractWaveTalk waveFlow) {
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
