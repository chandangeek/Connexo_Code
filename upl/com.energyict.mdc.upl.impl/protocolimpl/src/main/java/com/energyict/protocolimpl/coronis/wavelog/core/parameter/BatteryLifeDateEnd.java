package com.energyict.protocolimpl.coronis.wavelog.core.parameter;

import com.energyict.mdc.upl.UnsupportedException;
import com.energyict.protocolimpl.coronis.core.TimeDateRTCParser;
import com.energyict.protocolimpl.coronis.wavelog.WaveLog;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.util.Calendar;

public class BatteryLifeDateEnd extends AbstractParameter {

	private Calendar calendar;


	final Calendar getCalendar() {
		return calendar;
	}

	BatteryLifeDateEnd(WaveLog waveLog) {
		super(waveLog);
	}


	@Override
	ParameterId getParameterId() {
		return ParameterId.BatteryLifeDateEnd;
	}

	@Override
    protected void parse(byte[] data) throws IOException {

		long date = ProtocolUtils.getLong(data, 0, 7);
		if (date == 0x01010101010101L) {
			calendar = null;
		}
		else {
			calendar = TimeDateRTCParser.parse(data, getWaveLog().getTimeZone());
		}
	}

	@Override
    protected byte[] prepare() throws IOException {
		throw new UnsupportedException();
	}
}