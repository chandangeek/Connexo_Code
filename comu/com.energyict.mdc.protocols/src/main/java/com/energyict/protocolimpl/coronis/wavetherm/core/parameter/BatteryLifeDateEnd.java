package com.energyict.protocolimpl.coronis.wavetherm.core.parameter;

import com.energyict.protocols.util.ProtocolUtils;
import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.protocolimpl.coronis.core.TimeDateRTCParser;
import com.energyict.protocolimpl.coronis.wavetherm.WaveTherm;

import java.io.IOException;
import java.util.Calendar;

public class BatteryLifeDateEnd extends AbstractParameter {

	private Calendar calendar;


	final Calendar getCalendar() {
		return calendar;
	}

	BatteryLifeDateEnd(WaveTherm waveTherm) {
		super(waveTherm);
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
			calendar = TimeDateRTCParser.parse(data, getWaveTherm().getTimeZone());
		}
	}

	@Override
    protected byte[] prepare() throws IOException {
		throw new UnsupportedException();
	}
}